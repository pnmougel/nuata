package categories

import languages.NameWithLanguage

/**
 * Created by nico on 02/10/15.
 * Queries for the category controller
 */

/**
 *
 * @param name
 * @param description
 * @param names
 * @param forceInsert
 */
case class CategoryQuery(
              name: String,
              description: Option[String],
              names: Option[List[NameWithLanguage]],
              forceInsert: Option[Boolean])
