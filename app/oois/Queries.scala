package oois

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
 * @param unit
 * @param forceInsert
 */
case class OOICreateQuery(
              name: String,
              description: Option[String],
              names: Option[List[NameWithLanguage]],
              unit: String,
              forceInsert: Option[Boolean])
