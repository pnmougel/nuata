package oois

import languages.{Language, NameWithLanguage}
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
    val id = OOI.insert(query)
    Json.obj("id" -> id, "created" -> true)
  }

  /**
   * Create a new object of interest
   */
  def create = Action(parse.json) { implicit rs =>
    rs.body.validate[OOICreateQuery].map { query =>
      val forceInsert = query.forceInsert.getOrElse(false)
      val jsonRet = if(forceInsert) {
        doInsert(query)
      } else {
        val namesWithLanguageIds = query.names.map(name => (name.name, Language.getOrCreate(name.lang)))
        val oois = OOI.findByNames(namesWithLanguageIds)
        if(oois.isEmpty) {
          doInsert(query)
        } else {
          if(oois.length == 1) {
            Json.obj("created" -> false, "id" -> oois(0), "entry" -> toJson(oois(0)))
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