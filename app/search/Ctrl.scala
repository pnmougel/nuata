package search

import _root_.services.caches._
import facts.Fact
import play.api.db.DB
import play.api.libs.json._
import play.api.mvc._
import search.services._

/**
 * Created by nico on 01/10/15.
 *
 * Controller for the categories
 */
class Ctrl extends Controller {

  /**
   * Perform a data query
   * @return
   */
  def find(query: String) = Action {
    NameCache.build()
    OoiUnitCache.build()
    LanguageCache.build()

    val queryLower = query.toLowerCase.trim()
    val oois = NameCache.findOOIs(queryLower, "")
    val dimensions = NameCache.findDimensions(queryLower, "")

    val facts = Fact.findFacts(oois, dimensions)
    val factsJson = facts.map { fact =>
      val valueJson = fact._1.value match {
        case Left(value) => Json.obj("value" -> value)
        case Right(value) => Json.obj("value" -> value)
      }
      valueJson ++ Json.obj(
        "ooi" -> Json.obj(
          "id" -> fact._2,
          "unit" -> OoiUnitCache.getUnit(fact._2)
        ),
        "dimensions" -> fact._3
      )
    }
    Ok(Json.toJson(factsJson))
  }

  def clearDb = Action {
    import play.api.db._
    import anorm._
    import anorm.SqlParser._
    import play.api.Play.current

    val tableNames = List(
      "category", "category_name",
      "dimension", "dimension_name", "dimension_category", "dimension_relation", "dimension_relation_type",
      "fact", "fact_dimension", "fact_source",
      "language",
      "ooi", "ooi_name",
      "source", "unit")

    DB.withConnection { implicit c =>
      for(tableName <- tableNames) {
        SQL(s"TRUNCATE $tableName CASCADE").execute
      }
    }
    Ok("Done")
  }
}