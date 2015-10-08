package services

import play.api.libs.json.Json
import play.api.libs.json.Json._
import services.Categories.Category
import services.Lang._

/**
 * Created by nico on 02/10/15.
 *
 */
object Dimensions extends ServerConnection {
  case class Dimension(names: Seq[(String, Lang)],
                       categories: List[Category], description: Option[String] = None) extends ServerQuery {
    def asJson = {
      Json.obj(
        "description" -> description,
        "categories" -> categories.map(c => c.id.get),
        "names" -> names.map(name => Json.obj("name" -> name._1.replaceAll("'", ""), "lang" -> name._2))
      )
    }
  }
  case class DimensionResponse(isCreated: Boolean, matchingIds: List[Long]) extends ServerResponse

  def registerDimension(names: Seq[(String, Lang)], categories: List[Category], description: Option[String] = None) = {
    val newItem = Dimension(names, categories, description)
    items = newItem :: items
    newItem
  }

  def parseResponse(body: String) = {
    implicit val jsonResponseFormat = Json.format[DimensionResponse]
    Json.parse(body).as[List[DimensionResponse]]
  }

  def update() = {
    doUpdate("dimension")
  }
}
