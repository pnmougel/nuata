package controllers

import com.sksamuel.elastic4s.ElasticDsl._
import elasticsearch.ElasticSearch
import play.api.libs.json.Json
import play.api.mvc._
import repositories.{OoiRepository, FactRepository, DimensionRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Search extends Controller  {

  def searchFacts(query: String) = Action.async { implicit rs =>
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
