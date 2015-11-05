package repositories

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.jackson.ElasticJackson.Implicits._
import models.{CategoryModel, DimensionModel}
import org.elasticsearch.action.search.SearchResponse
import org.json4s._
import play.api.libs.json.{JsValue, Json}
import queries.DimensionQuery
import repositories.commons.LocalizedNamedItemRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by nico on 02/11/15.
 */
object DimensionRepository extends LocalizedNamedItemRepository[DimensionModel]("dimension") {
  implicit val formats = DefaultFormats
  protected def jsToInstance(jValue: JValue) = jValue.extract[DimensionModel]

  def resultToEntity(res: SearchResponse) = res.as[DimensionModel]
}