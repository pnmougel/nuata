package querybuilder

/**
 * Created by nico on 20/10/15.
 */
case class LocalizedString(str: String, lang: String)

object Lang {
  def en(str: String*): List[LocalizedString] = {
    str.toList.map( v => LocalizedString(v, "en"))
  }

  def fr(str: String*): List[LocalizedString] = {
    str.toList.map( v => LocalizedString(v, "fr"))
  }
}
