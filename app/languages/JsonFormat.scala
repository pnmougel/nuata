package languages

import play.api.libs.json.Json
import play.api.libs.json.Json._

/**
 * Created by nico on 01/10/15.
 * JSon format for the categories
 */
object JsonFormat {
  implicit val jsonNameWithLanguageFormat = Json.format[NameWithLanguage]
}
