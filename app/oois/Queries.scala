package oois

import languages.NameWithLanguage

/**
 * Created by nico on 02/10/15.
 * Queries for the category controller
 */

/**
 *
 * @param description
 * @param names
 * @param unit
 * @param forceInsert
 */
case class OOICreateQuery(
              description: Option[String],
              names: List[NameWithLanguage],
              unit: String,
              forceInsert: Option[Boolean])
