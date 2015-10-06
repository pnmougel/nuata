package search

import dimensions.Dimension
import facts.Fact
import oois.OOI
import play.api.libs.json._
import play.api.mvc._

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
    val elems = query.split(" ")
    val oois = OOI.findByNames(elems)
    val dimensions = Dimension.findDimensionByNames(elems)

    val facts = Fact.findFacts(oois, dimensions.map(_._1))

    val factsJson = facts.map { fact =>
      val valueJson = fact._1.value match {
        case Left(value) => Json.obj("value" -> value)
        case Right(value) => Json.obj("value" -> value)
      }
      valueJson ++ Json.obj(
        "ooi" -> Json.obj(
          "id" -> fact._2._1,
          "name" -> fact._2._2,
          "unit" -> fact._2._3
        ),
        "dimensions" -> fact._3
      )
    }

    Ok(Json.toJson(factsJson))
  }
}