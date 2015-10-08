package dimensions

import languages.NameWithLanguage

/**
 * Created by nico on 02/10/15.
 * Queries for the dimension controller
 */

/**
 * Query to create a dimension
 *
 * @param names Names of the dimension used for search
 * @param categories Id of the category of the created dimension
 * @param description A text describing the dimension
 * @param forceInsert If true, force the creation of the dimension even if a similar dimension exists
 */
case class DimensionCreateQuery(names: List[NameWithLanguage],
                                categories: List[Long],
                                description: Option[String],
                                forceInsert: Option[Boolean])

/**
 * Query to search a dimension
 *
 * @param name
 * @param categoryId
 * @param categoryName
 * @param parentName
 * @param parentId
 */
case class DimensionSearchQuery(name: String,
                                categoryId: Option[List[Long]], categoryName: Option[String],
                                parentName: Option[String], parentId: Option[Long])
