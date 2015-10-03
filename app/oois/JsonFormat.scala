package oois

import play.api.libs.json.Json
import play.api.libs.json.Json._
import languages.JsonFormat._

/**
 * Created by nico on 01/10/15.
 * JSon format for the objects of interest
 */
object JsonFormat {
  implicit val jsonOOICreateFormat = Json.format[OOICreateQuery]
  implicit val jsonOOIFormat = Json.format[OOI]
}
