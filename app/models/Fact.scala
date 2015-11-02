package models

import java.util.Date

import com.sksamuel.elastic4s.ElasticDsl.index
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import elasticsearch.ElasticSearch
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by nico on 14/10/15.
 */
case class Fact(value: Option[Double], valueInt: Option[Long], at: Option[String], dimensions: List[String], ooi: String) extends ItemWithDependencies {

  var allDimensions = List[Dimension]()
  var curOOI: Option[OOI] = None

  def resolveDependencies(createQuery: CreateQuery): Unit = {
    allDimensions = for(dimensionRef <- dimensions; dimension <- createQuery.dimensionRefMapping.get(dimensionRef.toLowerCase)) yield dimension
    curOOI = createQuery.ooiRefMapping.get(ooi.toLowerCase)
  }

  def searchItem(): Future[SearchResult] = {
    getSearchIds(allDimensions).flatMap { dimensionOptIds =>
      val dimensionIds = for(idOpt <- dimensionOptIds; id <- idOpt) yield { id }
      getSearchIds(List(curOOI.get)).flatMap { ooiIdOpt =>
        val ooiId = for(idOpt <- ooiIdOpt; id <- idOpt) yield { id }
        var values = Map[String, Any](
          "dimensionIds" -> dimensionIds,
          "ooiId" -> ooiId)
        for(valueL <- valueInt) { values += ("valueInt" -> valueL) }
        for(valueD <- value) { values += ("value" -> valueD) }
        val query = index into "nuata" / "fact" fields values
        ElasticSearch.client.execute { query }.map { res =>
          val source = Json.parse(query._fieldsAsXContent.string)
          SearchResult(Some(res.getId), Some(source), isCreated = true, nbHits = 0, Seq(), idGivenButMissing = false)
        }
      }
    }
  }
}
