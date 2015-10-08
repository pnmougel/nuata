package categories

import languages._
import languages.JsonFormat._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Json._
import JsonFormat._

import scala.collection._

/**
 * Created by nico on 01/10/15.
 * Controller for the categories
 */

class Ctrl extends Controller {
  /*
  /**
   * Perform the insertion of a category
   * @param query
   * @return
   */
  private def doInsert(query: CategoryQuery) : JsObject = {
    val id = Category.insert(query.name, query.description, query.names)
    Json.obj("id" -> id, "created" -> true)
  }
  */

  /*
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
  */


  def create = Action(parse.json) { implicit rs =>
    case class ResponseToCreateCategory(var id: Option[Long], isCreated: Boolean, query: CategoryQuery, matchingIds: List[Long])

    rs.body.validate[Array[CategoryQuery]].map { queries =>
      // List of the queries that will be inserted in the DB
      var queriesToInsert = List[CategoryQuery]()

      val queryResponses = for(query <- queries) yield {
        // Check if the item already exists or not
        val namesWithLanguageIds = query.names.map(name => (name.name, Language.getOrCreate(name.lang)))
        val matchingCategoriesId = Category.findByNames(namesWithLanguageIds)

        // If the item does not exists, add it to list of queries to insert
        if(matchingCategoriesId.isEmpty) {
          queriesToInsert = query :: queriesToInsert
        }

        // Get the id if there is a single match
        val id = if(matchingCategoriesId.length == 1) {
          Some(matchingCategoriesId(0))
        } else { None }
        val isCreated = matchingCategoriesId.isEmpty
        ResponseToCreateCategory(id, isCreated, query, matchingCategoriesId)
      }
      val ids = Category.batchInsert(queriesToInsert)
      val queryToInsertToId = Map[CategoryQuery, Long](queriesToInsert.zip(ids) : _*)

      // Build the response
      val responses = for(queryResponse <- queryResponses) yield {
        val id = if(queryResponse.isCreated) {
          Some(queryToInsertToId(queryResponse.query))
        } else { queryResponse.id }
        Json.obj(
          "id" -> id,
          "names" -> Json.toJson(queryResponse.query.names),
          "isCreated" -> queryResponse.isCreated,
          "matchingIds" -> queryResponse.matchingIds
          )
      }
      Ok(Json.toJson(responses))
    }.getOrElse(BadRequest("invalid json"))
  }

  def findByName(name: String) = Action {
    val categories = Category.findByName(name)
    Ok(toJson(categories))
  }
}