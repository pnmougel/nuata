package categories

import play.api.libs.json.Json
import play.api.libs.json.Json._
import languages.JsonFormat._

/**
 * Created by nico on 01/10/15.
 * JSon format for the categories
 */
object JsonFormat {
  implicit val jsonCategoryQueryFormat = Json.format[CategoryQuery]
  implicit val jsonCategoryFormat = Json.format[Category]
}
