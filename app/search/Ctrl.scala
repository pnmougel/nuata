package search

import dimensions.Dimension
import facts.Fact
import oois.OOI
import play.api.libs.json._
import play.api.mvc._
import services.caches.{OoiUnitCache, NameCache}
import services.timers.Timer

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
    NameCache.buildCache()
    OoiUnitCache.buildCache()

    val queryLower = query.toLowerCase().trim()
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
}