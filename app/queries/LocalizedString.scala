package queries // Combinator syntax
// Combinator syntax

/**
 * Created by nico on 13/10/15.
 */

/**
 * List of the available languages
 */
object AvailableLanguages {
  val languages = Map(
    "fr" -> Language("fr"),
    "en" -> Language("en"))
}

case class Language(name: String)
case class LocalizedString(value: String, lang: String)
case class LocalizedName(name: String, lang: String)
case class LocalizedDescription(description: String, lang: String)


/*
object LocalizedStringReaders {
  def getLanguage(implicit r: Reads[String]): Reads[Language] = r.filter(ValidationError("Unknown language"))(language => {
    AvailableLanguages.languages.contains(language)
  }).map(lang => {
    AvailableLanguages.languages(lang)
  })

//  implicit val langRead: Reads[Language] = __.read[Language](getLanguage)
//  implicit val localizedStringReader = Json.reads[LocalizedString]
//  implicit val localizedNameReader = Json.reads[LocalizedName]
//  implicit val localizedDescriptionReader = Json.reads[LocalizedDescription]
}
*/

/*
object Test {
  def main(args: Array[String]) {

//    val strArr = """[{"lang": "other"}, {"lang": "fr"}, {"lang": "test"}] """
    val str = """{"name": "Bla bla bla", "lang": "fr2"}"""
    val js = Json.parse(str)
    import LocalizedStringReaders._

    js.validate[LocalizedName] match {
      case s: JsSuccess[LocalizedName] => {
        println(s.get)
      }
      case e: JsError => {

        println(Json.prettyPrint(JsError.toJson(e)))
      }
    }
  }


}
*/

