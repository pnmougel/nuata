package facts

import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Json._
import JsonFormat._

/**
 * Created by nico on 01/10/15.
 *
 * Controller for the categories
 */
class Ctrl extends Controller {

  /**
   * Create a new fact
   * @return
   */
  def create = Action(parse.json) { implicit rs =>
    rs.body.validate[FactCreateQuery].map { query =>
      val value = if(query.value % 1 == 0) {
        Left(query.value.toLong)
      } else {
        Right(query.value)
      }
      val id = Fact.insert(value, query.ooi, query.dimensions)
      Ok(Json.obj("id" -> id))
    }.getOrElse(BadRequest("invalid json"))
  }
}