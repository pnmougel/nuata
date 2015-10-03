package dimensions

import categories.Category
import languages.NameWithLanguage
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

  private def doInsert(query: DimensionCreateQuery) = {
    val id = Dimension.insert(query.name, query.categories, query.description, query.names.getOrElse(List[NameWithLanguage]()))
    Json.obj("id" -> id, "created" -> true)
  }

  def create = Action(parse.json) { implicit rs =>
    rs.body.validate[DimensionCreateQuery].map { query =>
      val jsonRet = if(query.forceInsert.getOrElse(false)) {
        doInsert(query)
      } else {
        val dimensions = Dimension.findDimension(query.name, query.categories)
        if(dimensions.isEmpty) {
          println("Empty ?")
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
}