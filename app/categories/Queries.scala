package categories

import languages.NameWithLanguage

/**
 * Created by nico on 02/10/15.
 * Queries for the category controller
 */

/**
 *
 * @param description
 * @param names
 */
case class CategoryQuery(
              description: Option[String],
              names: List[NameWithLanguage]) {
}
