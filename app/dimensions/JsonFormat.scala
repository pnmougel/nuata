package dimensions

import play.api.libs.json.Json
import play.api.libs.json.Json._
import languages.JsonFormat._

/**
 * Created by nico on 01/10/15.
 * JSon format for the dimensions
 */
object JsonFormat {
  implicit val jsonDimensionCreateQueryFormat = Json.format[DimensionCreateQuery]
  implicit val jsonDimensionSearchQueryFormat = Json.format[DimensionSearchQuery]
  implicit val jsonDimensionFormat = Json.format[Dimension]
}
