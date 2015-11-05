package repositories

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.jackson.ElasticJackson.Implicits._
import models.{CategoryModel, OoiModel}
import org.elasticsearch.action.search.SearchResponse
import org.json4s._
import play.api.libs.json.{JsValue, Json}
import repositories.DimensionRepository._
import repositories.commons.LocalizedNamedItemRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by nico on 02/11/15.
 */
object OoiRepository extends LocalizedNamedItemRepository[OoiModel]("ooi") {
  implicit val formats = DefaultFormats
  protected def jsToInstance(jValue: JValue) = jValue.extract[OoiModel]

  def resultToEntity(res: SearchResponse) = res.as[OoiModel]

}
