package repositories

import models.{UnitModel, CategoryModel, DimensionModel, FactModel}
import org.elasticsearch.action.search.SearchResponse
import org.json4s._
import repositories.commons.LocalizedNamedItemRepository
import com.sksamuel.elastic4s.jackson.ElasticJackson.Implicits._
import scala.concurrent.ExecutionContext.Implicits.global

import com.sksamuel.elastic4s.ElasticDsl._

import scala.concurrent.Future

/**
 * Created by nico on 02/11/15.
 */

object FactRepository extends LocalizedNamedItemRepository[FactModel]("fact") {
  implicit val formats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all

  def resultToEntity(res: SearchResponse) = res.as[FactModel]

  protected def jsToInstance(jValue: JValue) = jValue.extract[FactModel]

  def indexFacts(facts: List[FactModel]): Future[Array[String]] = {
    val indexQueries = facts.map( fact => {
      var indexQuery = Map[String, Any]("dimensionIds" -> fact.dimensionIds, "ooiIds" -> fact.ooiIds, "sourceIds" -> fact.sourceIds)
      for(v <- fact.value) { indexQuery += ("value" -> v) }
      for(v <- fact.valueInt) { indexQuery += ("valueInt" -> v) }
      for(v <- fact.at) { indexQuery += ("at" -> v) }
      index into path fields indexQuery
    })
    client.execute(bulk (indexQueries) ).map( results => {
      results.getItems.map( _.getId )
    })
  }
}
