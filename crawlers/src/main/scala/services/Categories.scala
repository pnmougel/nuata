package services

/*
import play.api.libs.json._
import play.api.libs.json.Json._

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
//  case class CategoryResponse(isCreated: Boolean, matchingIds: List[Long]) extends ServerResponse
//  implicit val jsonResponseFormat = Json.format[CategoryResponse]

  def registerCategory(names: Seq[(String, Lang)], description: Option[String] = None) = {
    val newItem = Category(names, description)
    items = newItem :: items
    newItem
  }

//  def parseResponse(body: String) = {
//    val res = Json.parse(body).as[List[CategoryResponse]]
//    for(r <- res) {
//      if(!r.id.isDefined && r.matchingIds.length == 1) {
//        r.id = Some(r.matchingIds(0))
//      }
//    }
//    res
//  }

  def update() = {
    doUpdate("category")
  }
}
*/