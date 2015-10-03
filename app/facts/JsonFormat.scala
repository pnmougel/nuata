package facts

import play.api.libs.json.Json
import play.api.libs.json.Json._

/**
 * Created by nico on 01/10/15.
 * JSon format for the facts
 */
object JsonFormat {
  // Queries
  implicit val jsonFactCreateQueryFormat = Json.format[FactCreateQuery]

  // Responses
//  implicit val jsonFactFormat = Json.format[FactValue]
}
