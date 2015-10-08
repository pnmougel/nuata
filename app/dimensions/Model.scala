package dimensions

import java.util.Date

import anorm.SqlParser._
import anorm._
import languages.{NameWithLanguage, Language}
import play.api.Play.current
import play.api.db._
import shared.ModelWithNames

/**
 * Created by nico on 01/10/15.
 *
 */
case class Dimension(id: Long, name: String, description: Option[String]) {}

object Dimension extends ModelWithNames("dimension") {
  val table = "Dimension"
  val tableNames = "Dimension_Name"

  val simple = {
    get[Long]("id") ~ get[String]("name") ~ get[Option[String]]("description") map {
      case id ~ name ~ description  => Dimension(id, name, description)
    }
  }

  val idWithNameParser = {
    get[Long]("dimension_id") ~ get[String]("name") map {
      case id ~ name  => (id, name)
    }
  }

  def findAll(): Seq[Dimension] = {
    DB.withConnection { implicit c =>
      SQL"SELECT * FROM #$table".as(Dimension.simple *)
    }
  }

  def findDimensionByNames(names: Array[String]) : List[(Long, String)] = {
    val namesLower = names.map(_.toLowerCase.trim).toList
    DB.withConnection { implicit c =>
      SQL"SELECT dimension_id, name FROM dimension_name WHERE name_search IN ($namesLower)".as(idWithNameParser.*)
    }
  }

  def findDimension(name: String, categoryId: List[Long], parentId: Option[Long] = None): List[Dimension] = {
    val nameSearch = name.trim.toLowerCase
    DB.withConnection { implicit c =>
      var filterQueries = List[String](s"(SELECT dimension_id FROM $tableNames WHERE name_search = {name})")
      for(id <- parentId) {
        filterQueries = "(SELECT child_id FROM dimension_relation WHERE parent_id = {parentId})" :: filterQueries
      }
      if(categoryId.nonEmpty) {
        filterQueries = "(SELECT dimension_id FROM dimension_category WHERE category_id IN ({categoryId}))" :: filterQueries
//        filterQueries = "(SELECT dimension_id FROM dimension_category WHERE category_id = 1)" :: filterQueries
      }
      val filterQuery = filterQueries.mkString("(", " INTERSECT ", ")")

      val query = s"""SELECT * FROM $table WHERE id IN $filterQuery"""
      SQL(query)
        .on('name -> nameSearch, 'categoryId -> categoryId, 'parentId -> parentId.getOrElse(0L))
        .as(Dimension.simple *)
    }
  }

  /*
  def insert(query: DimensionCreateQuery) = {
    val now = new Date()
    DB.withConnection { implicit c =>
      // Create the dimension entry
      val dimensionId: Option[Long] =
        SQL"""
          INSERT INTO #$table(name, description, created_at)
          VALUES  (${query.name}, ${query.description}, $now)
        """.executeInsert()

      // Add association to categories
      for(categoryId <- query.categories) {
        SQL"""
          INSERT INTO dimension_category (dimension_id, category_id)
          VALUES  ($dimensionId, $categoryId)
        """.executeInsert()
      }

      // Add names
      for(name <- NameWithLanguage(query.name, None) :: query.names.getOrElse(List[NameWithLanguage]())) {
        val languageId = for(languageCode <- name.language) yield { Language.getOrCreate(languageCode) }
        SQL"""
          INSERT INTO dimension_name
          (name, name_search, language_id, dimension_id)
          VALUES (${name.name}, ${name.nameSearch}, $languageId, $dimensionId)
        """.executeInsert()
      }
      dimensionId
    }
  }
  */


  def insert(queries: Seq[DimensionCreateQuery]) : Seq[Long] = {
    val now = new Date()
    DB.withTransaction { implicit c =>
      for(query <- queries) yield {
        val dimensionId: Option[Long] =
          SQL"""
          INSERT INTO #$table (description, created_at)
          VALUES  (${query.description}, $now)
        """.executeInsert()

        // Add association to categories
        for(categoryId <- query.categories) {
          SQL"""
          INSERT INTO dimension_category (dimension_id, category_id)
          VALUES  ($dimensionId, $categoryId)
        """.executeInsert()
        }

        // Add names
        for(name <- query.names) {
          val languageId = Language.getOrCreate(name.lang)
          SQL"""
          INSERT INTO dimension_name
          (name, name_search, language_id, dimension_id)
          VALUES (${name.name}, ${name.nameSearch}, $languageId, $dimensionId)
        """.executeInsert()
        }
        dimensionId.get
      }
    }
  }
//
//  def findByNames(names: Seq[(String, Long)]) : List[Long] = {
//    DB.withConnection { implicit c =>
//      val queryNameSearch = names.map(name => {
//        s"(name_search = '${name._1}' AND language_id = ${name._2})"
//      }).mkString(" OR ")
//      SQL(s"""
//        SELECT DISTINCT(dimension_id)
//        FROM dimension_name
//        WHERE $queryNameSearch
//      """).as(long("dimension_id") *)
//    }
//  }
}