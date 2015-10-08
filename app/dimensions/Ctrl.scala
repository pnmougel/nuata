package dimensions

import categories.{CategoryQuery, Category}
import languages.{Language, NameWithLanguage}
import languages.JsonFormat._
import play.api.libs.json.Json
import play.api.libs.json.Json._
import play.api.mvc._


/**
 * Created by nico on 01/10/15.
 */
class Ctrl extends Controller {
  import JsonFormat._

  def findDimensions = Action(parse.json) { implicit rs =>
    rs.body.validate[DimensionSearchQuery].map { query =>
      // Compute the ids of the categories
      val categoriesId = query.categoryId.getOrElse(List[Long]()) :::
        (for(categoryName <- query.categoryName) yield {
          Category.findIdByName(categoryName)
      }).getOrElse(List[Long]())

      Dimension.findDimension(query.name, categoriesId, query.parentId)
      Ok(toJson(query))
    }.getOrElse(BadRequest("invalid json"))
  }

  /*
  private def doInsert(query: DimensionCreateQuery) = {
    val id = Dimension.insert(query)
    Json.obj("id" -> id, "created" -> true)
  }


  def create = Action(parse.json) { implicit rs =>
    rs.body.validate[DimensionCreateQuery].map { query =>
      val jsonRet = if(query.forceInsert.getOrElse(false)) {
        doInsert(query)
      } else {
        val dimensions = Dimension.findDimension(query.name, query.categories)
        if(dimensions.isEmpty) {
          doInsert(query)
        } else {
          if(dimensions.length == 1) {
            Json.obj("created" -> false, "id" -> dimensions(0).id, "entry" -> toJson(dimensions(0)))
          } else {
            Json.obj("created" -> false, "entries" -> toJson(dimensions))
          }
        }
      }
      Ok(jsonRet)
    }.getOrElse(BadRequest("invalid json"))
  }
  */

//  def createBatch = Action(parse.json) { implicit rs =>
//    rs.body.validate[Array[DimensionCreateQuery]].map { queries =>
//      Dimension.insert(queries)
//      Ok(Json.obj("test" -> false))
//    }.getOrElse(BadRequest("invalid json"))
//  }


  def create = Action(parse.json) { implicit rs =>
    case class ResponseToCreateDimension(var id: Option[Long], isCreated: Boolean, query: DimensionCreateQuery, matchingIds: List[Long])

    rs.body.validate[Array[DimensionCreateQuery]].map { queries =>
      // List of the queries that will be inserted in the DB
      var queriesToInsert = List[DimensionCreateQuery]()

      val queryResponses = for(query <- queries) yield {
        // Check if the item already exists or not
        val namesWithLanguageIds = query.names.map(name => (name.name, Language.getOrCreate(name.lang)))
        val matchingIds = Dimension.findByNames(namesWithLanguageIds)

        // If the item does not exists, add it to list of queries to insert
        if(matchingIds.isEmpty) {
          queriesToInsert = query :: queriesToInsert
        }

        val id = if(matchingIds.length == 1) {
          Some(matchingIds(0))
        } else { None }
        val isCreated = matchingIds.isEmpty
        ResponseToCreateDimension(id, isCreated, query, matchingIds)
      }
      val ids = Dimension.insert(queriesToInsert)
      val queryToInsertToId = Map[DimensionCreateQuery, Long](queriesToInsert.zip(ids) : _*)

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
}