package controllers

import com.github.tototoshi.play2.json4s.jackson.Json4s
import com.sksamuel.elastic4s.ElasticDsl._
import elasticsearch.ElasticSearch
import models.FactModel
import org.json4s.{DefaultFormats, Extraction}
import play.api.libs.json.Json
import play.api.mvc._
import repositories.{QueryRepository, OoiRepository, FactRepository, DimensionRepository}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.sksamuel.elastic4s.jackson.ElasticJackson.Implicits._

class Search extends Controller with Json4s {
  implicit val formats = DefaultFormats

  val validItemTypes = Map("category" -> "categoryIds", "dimension" -> "dimensionIds", "unit" -> "", "ooi" -> "ooiIds")

  def searchFacts(query: String, dimensionIds: List[String], categoryIds: List[String], unitIds: List[String], ooiIds: List[String]) = Action.async { implicit rs =>
    // Log the query
    QueryRepository.logQuery(query, dimensionIds, categoryIds, unitIds, ooiIds, rs.remoteAddress)

    val defaultTermQueries = List(
      filteredQuery filter termsFilter("dimensionIds", dimensionIds : _*),
      filteredQuery filter termsFilter("ooiIds", ooiIds : _*)
    )

    val esQuery = search in "nuata" / "fact" query bool { must(defaultTermQueries)}

    ElasticSearch.client.execute { esQuery }.flatMap( facts => {
      val xFactModels = facts.as[FactModel]
      val factModels = (for(fact <- xFactModels) yield {
        val isAllDimensionsInQuery = !fact.dimensionIds.exists( dimensionId => !dimensionIds.contains(dimensionId) )
        (fact, isAllDimensionsInQuery)
      }).filter(_._2).reverse.map(_._1)

      /*
      val factModels = (for(fact <- facts.hits.toList) yield {
        val factModel = FactRepository.fromJsonStr(fact.sourceAsString)
        val isAllDimensionsInQuery = !factModel.dimensionIds.exists( dimensionId => !dimensionIdToHit.contains(dimensionId) )
        val scores = for(dimensionId <- factModel.dimensionIds; dimension <- dimensionIdToHit.get(dimensionId)) yield {
          dimension.getScore
        }
        (factModel, isAllDimensionsInQuery, scores.max)
      }).filter(_._2).sortBy(_._3).reverse.map(_._1)
      */
      Future.sequence(for(factModel <- factModels.toList) yield { factModel.toJson }).map( facts => {
        Ok(Extraction.decompose(facts))
      })
    })


    /*
    val dimensionsRes = DimensionRepository.nameContaining(query)
    val ooisRes = OoiRepository.nameContaining(query)

    dimensionsRes.flatMap( dimensions => {
      ooisRes.flatMap(oois => {
        val dimensionIdToHit = Map((for(dimensionHit <- dimensions.getHits.getHits) yield { dimensionHit.id() -> dimensionHit }) : _*)
        val ooiIdToHit = Map((for(ooiHit <- oois.getHits.getHits) yield { ooiHit.id() -> ooiHit }) : _*)

        val defaultTermQueries = List(
          filteredQuery filter termsFilter("dimensionIds", dimensionIdToHit.keys.toList : _*),
          filteredQuery filter termsFilter("ooiIds", ooiIdToHit.keys.toList : _*)
        )

        val esQuery = search in "nuata" / "fact" query bool { must(defaultTermQueries)}

        ElasticSearch.client.execute { esQuery }.flatMap( facts => {
          val xFactModels = facts.as[FactModel]
          val factModels = (for(fact <- xFactModels) yield {
            val isAllDimensionsInQuery = !fact.dimensionIds.exists( dimensionId => !dimensionIdToHit.contains(dimensionId) )
            val scores = for(dimensionId <- fact.dimensionIds; dimension <- dimensionIdToHit.get(dimensionId)) yield {
              dimension.getScore
            }
            (fact, isAllDimensionsInQuery, scores.max)
          }).filter(_._2).sortBy(_._3).reverse.map(_._1)

          /*
          val factModels = (for(fact <- facts.hits.toList) yield {
            val factModel = FactRepository.fromJsonStr(fact.sourceAsString)
            val isAllDimensionsInQuery = !factModel.dimensionIds.exists( dimensionId => !dimensionIdToHit.contains(dimensionId) )
            val scores = for(dimensionId <- factModel.dimensionIds; dimension <- dimensionIdToHit.get(dimensionId)) yield {
              dimension.getScore
            }
            (factModel, isAllDimensionsInQuery, scores.max)
          }).filter(_._2).sortBy(_._3).reverse.map(_._1)
          */
          Future.sequence(for(factModel <- factModels.toList) yield { factModel.toJson }).map( facts => {
            Ok(Extraction.decompose(facts))
          })
        })
      })
    })
    */
  }
}
