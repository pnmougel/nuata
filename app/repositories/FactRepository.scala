package repositories

import models.{DimensionModel, FactModel}
import org.elasticsearch.action.search.SearchResponse
import org.json4s._
import repositories.commons.LocalizedNamedItemRepository
import com.sksamuel.elastic4s.jackson.ElasticJackson.Implicits._
import scala.concurrent.ExecutionContext.Implicits.global

import com.sksamuel.elastic4s.ElasticDsl._

/**
 * Created by nico on 02/11/15.
 */

object FactRepository extends LocalizedNamedItemRepository[FactModel]("fact") {
  implicit val formats = DefaultFormats
  protected def jsToInstance(jValue: JValue) = jValue.extract[FactModel]

  def resultToEntity(res: SearchResponse) = res.as[FactModel]
}
