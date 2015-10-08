package services

import play.api.libs.json._
import play.api.libs.json.Json._
import services.Lang.Lang

/**
 * Created by nico on 01/10/15.
 */
object Categories extends ServerConnection {

  case class Category(names: Seq[(String, Lang)], description: Option[String]) extends ServerQuery {
    def asJson = {
      Json.obj(
        "description" -> description,
        "names" -> names.map(name => Json.obj("name" -> name._1, "lang" -> name._2))
      )
    }
  }
  case class CategoryResponse(isCreated: Boolean, matchingIds: List[Long]) extends ServerResponse

  def registerCategory(names: Seq[(String, Lang)], description: Option[String] = None) = {
    val newItem = Category(names, description)
    items = newItem :: items
    newItem
  }

  def parseResponse(body: String) = {
    implicit val jsonResponseFormat = Json.format[CategoryResponse]
    Json.parse(body).as[List[CategoryResponse]]
  }

  def update() = {
    doUpdate("category")
  }
}
