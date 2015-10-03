package oois

import languages.NameWithLanguage
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Json._
import oois.JsonFormat._
/**
 * Created by nico on 01/10/15.
 * Controller for the categories
 */

class Ctrl extends Controller {
  /**
   * Perform the insertion of a category
   * @param query
   * @return
   */
  private def doInsert(query: OOICreateQuery) : JsObject = {
    val id = OOI.insert(query.name, query.unit, query.description, query.names.getOrElse(List[NameWithLanguage]()))
    Json.obj("id" -> id, "created" -> true)
  }

  /**
   * Create a new object of interest
   * If forceInsert is true, the category will always be created
   * otherwise
   * - if a single category with the same name exists the id of this category is returned and the category is not created
   * - if a several categories with the same name exists the ids of these categories are returned and the category is not created
   * - if there is no categories with a matching name, the category is created
   * @return
   */
  def create = Action(parse.json) { implicit rs =>
    rs.body.validate[OOICreateQuery].map { query =>
      val forceInsert = query.forceInsert.getOrElse(false)
      val jsonRet = if(forceInsert) {
        doInsert(query)
      } else {
        val oois = OOI.findByName(query.name)
        if(oois.isEmpty) {
          doInsert(query)
        } else {
          if(oois.length == 1) {
            Json.obj("created" -> false, "id" -> oois(0).id, "entry" -> toJson(oois(0)))
          } else {
            Json.obj("created" -> false, "entries" -> toJson(oois))
          }
        }
      }
      Ok(jsonRet)
    }.getOrElse(BadRequest("invalid json"))
  }

  def findByName(name: String) = Action {
    val items = OOI.findByName(name)
    Ok(toJson(items))
  }
}