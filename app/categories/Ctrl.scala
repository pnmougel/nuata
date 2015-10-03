package categories

import languages.NameWithLanguage
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Json._
import JsonFormat._

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
  private def doInsert(query: CategoryQuery) : JsObject = {
    val id = Category.insert(query.name, query.description, query.names.getOrElse(List[NameWithLanguage]()))
    Json.obj("id" -> id, "created" -> true)
  }

  /**
   * Create a new category
   * If forceInsert is true, the category will always be created
   * otherwise
   * - if a single category with the same name exists the id of this category is returned and the category is not created
   * - if a several categories with the same name exists the ids of these categories are returned and the category is not created
   * - if there is no categories with a matching name, the category is created
   * @return
   */
  def create = Action(parse.json) { implicit rs =>
    rs.body.validate[CategoryQuery].map { query =>
      val forceInsert = query.forceInsert.getOrElse(false)
      val jsonRet = if(forceInsert) {
        doInsert(query)
      } else {
        val categories = Category.findByName(query.name)
        if(categories.isEmpty) {
          doInsert(query)
        } else {
          if(categories.length == 1) {
            Json.obj("created" -> false, "id" -> categories(0).id, "entry" -> toJson(categories(0)))
          } else {
            Json.obj("created" -> false, "entries" -> toJson(categories))
          }
        }
      }
      Ok(jsonRet)
    }.getOrElse(BadRequest("invalid json"))
  }

  def findByName(name: String) = Action {
    val categories = Category.findByName(name)
    Ok(toJson(categories))
  }
}