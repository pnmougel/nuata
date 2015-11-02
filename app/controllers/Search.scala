package controllers

import java.util

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import elasticsearch.ElasticSearch
import org.json4s.ext.EnumSerializer
import models._
import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.json4s._

/**
 * Created by nico on 30/10/15.
 */

object DimensionRepository extends BaseRepository[DimensionModel]("dimension") {
  implicit val formats = DefaultFormats
  protected def jsToInstance(jValue: JValue) = jValue.extract[DimensionModel]
}

case class DimensionModel(names: Map[String, List[String]], descriptions: Map[String, List[String]], categoryIds: List[String]) {
  val categories = Future.sequence(for(categoryId <- categoryIds) yield { CategoryRepository.byId(categoryId) })

  def toJson : Future[JsObject] = {
    categories.flatMap( items => { Future.sequence(items.map( item => { item.toJson.map(itemJson => itemJson) })) }).map( items => {
      Json.obj(
        "names" -> names,
        "descriptions" -> descriptions,
        "categories" -> Json.arr(items)
      )
    })
  }
}

object OoiRepository extends BaseRepository[OoiModel]("ooi") {
  implicit val formats = DefaultFormats
  protected def jsToInstance(jValue: JValue) = jValue.extract[OoiModel]
}
case class OoiModel(names: Map[String, List[String]], descriptions: Map[String, List[String]], unitIds: List[String]) {
  val units = Future.sequence(for(unitId <- unitIds) yield { UnitRepository.byId(unitId) })

  def toJson : Future[JsObject] = {
    units.flatMap( items => { Future.sequence(items.map( item => { item.toJson.map(itemJson => itemJson) })) }).map( items => {
      Json.obj(
        "names" -> names,
        "descriptions" -> descriptions,
        "units" -> Json.arr(items)
      )
    })
  }
}

object CategoryRepository extends BaseRepository[CategoryModel]("category") {
  implicit val formats = DefaultFormats
  protected def jsToInstance(jValue: JValue) = jValue.extract[CategoryModel]
}
case class CategoryModel(names: Map[String, List[String]], descriptions: Map[String, List[String]]) {
  def toJson : Future[JsObject] = {
    Future(Json.obj(
      "names" -> names,
      "descriptions" -> descriptions
    ))
  }
}


object UnitRepository extends BaseRepository[UnitModel]("unit") {
  implicit val formats = DefaultFormats
  protected def jsToInstance(jValue: JValue) = jValue.extract[UnitModel]
}
case class UnitModel(names: Map[String, List[String]], descriptions: Map[String, List[String]]) {
  def toJson : Future[JsObject] = {
    Future(Json.obj(
      "names" -> names,
      "descriptions" -> descriptions
    ))
  }
}


object FactRepository extends BaseRepository[FactModel]("fact") {
  implicit val formats = DefaultFormats
  protected def jsToInstance(jValue: JValue) = jValue.extract[FactModel]
}

case class FactModel(ooiId: List[String], dimensionIds: List[String], value: Option[Double], valueInt: Option[Long]) {
  val oois = Future.sequence(for(ooiId <- ooiId) yield { OoiRepository.byId(ooiId) })
  val dimensions = Future.sequence(for(dimensionId <- dimensionIds) yield { DimensionRepository.byId(dimensionId) })

  def toJson: Future[JsObject] = {
    dimensions.flatMap( items => { Future.sequence(items.map( item => { item.toJson.map(itemJson => itemJson) })) }).flatMap( dimensionsJson => {
      oois.flatMap( items => { Future.sequence(items.map( item => { item.toJson.map(itemJson => itemJson) })) }).map( ooisJson => {
        Json.obj(
          "value" -> value,
          "valueInt" -> valueInt,
          "dimensions" -> Json.arr(dimensionsJson),
          "oois" -> Json.arr(ooisJson)
        )
      })
    })
  }
}

abstract class BaseRepository[T](`type`: String) {
  def byId(id: String): Future[T] = {
    ElasticSearch.client.execute {get id id from "nuata" / `type`}.map(item => {
      fromJsonStr(item.getSourceAsString)
    })
  }

  protected def jsToInstance(jValue: JValue): T

  def fromJsonStr(jsonStr: String): T = {
    jsToInstance(org.json4s.jackson.JsonMethods.parse(jsonStr))
  }
}

class Search extends Controller  {

//  implicit val formats = DefaultFormats + new EnumSerializer(CreateOption)
//  implicit val searchResultFormat = Json.writes[SearchResult]

  def searchFacts(query: String) = Action.async { implicit rs =>
    val dimensionQuery = search in "nuata" / "dimension" query nestedQuery("names").query(
      bool {
        should {
          Seq(matchQuery("en", query), matchQuery("fr", query))
        }
      }
    ) highlighting(
      highlight field "names.en" fragmentSize(0))
    val ooiQuery = search in "nuata" / "ooi" query nestedQuery("names").query(
      bool {
        should {
          Seq(matchQuery("en", query), matchQuery("fr", query))
        }
      }
    ) highlighting(
      highlight field "names.en" fragmentSize(0))

    val dimensionsRes = ElasticSearch.client.execute { dimensionQuery }
    val ooisRes = ElasticSearch.client.execute { ooiQuery }

    dimensionsRes.flatMap( dimensions => {
      ooisRes.flatMap(oois => {
        val dimensionIdToHit = Map((for(dimensionHit <- dimensions.getHits.getHits) yield { dimensionHit.id() -> dimensionHit }) : _*)
        val ooiIdToHit = Map((for(ooiHit <- oois.getHits.getHits) yield { ooiHit.id() -> ooiHit }) : _*)


        val defaultTermQueries = List(
          filteredQuery filter termsFilter("dimensionIds", dimensionIdToHit.keys.toList : _*),
          filteredQuery filter termsFilter("ooiId", ooiIdToHit.keys.toList : _*)
        )

        val esQuery = search in "nuata" / "fact" query bool { must(defaultTermQueries)}

        ElasticSearch.client.execute { esQuery }.flatMap( facts => {
          val factModels = (for(fact <- facts.hits.toList) yield {
            val factModel = FactRepository.fromJsonStr(fact.sourceAsString)
            val isAllDimensionsInQuery = !factModel.dimensionIds.exists( dimensionId => !dimensionIdToHit.contains(dimensionId) )
            val scores = for(dimensionId <- factModel.dimensionIds; dimension <- dimensionIdToHit.get(dimensionId)) yield {
              dimension.getScore
            }
            (factModel, isAllDimensionsInQuery, scores.max)
          }).filter(_._2).sortBy(_._3).reverse.map(_._1)

          Future.sequence(for(factModel <- factModels) yield { factModel.toJson }).map( facts => {
            Ok(Json.toJson(facts))
          })
        })
      })
    })
  }
}
