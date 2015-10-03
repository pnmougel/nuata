package dimensions

import java.util.Date

import anorm.SqlParser._
import anorm._
import languages.{NameWithLanguage, Language}
import play.api.Play.current
import play.api.db._

/**
 * Created by nico on 01/10/15.
 *
 *
 */
case class Dimension(id: Long, name: String, description: Option[String]) {}

object Dimension {
  val table = "Dimension"
  val tableNames = "Dimension_Name"

  val simple = {
    get[Long]("id") ~ get[String]("name") ~ get[Option[String]]("description") map {
      case id ~ name ~ description  => Dimension(id, name, description)
    }
  }

  def findAll(): Seq[Dimension] = {
    DB.withConnection { implicit c =>
      SQL"SELECT * FROM #$table".as(Dimension.simple *)
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
      println(query)
      SQL(query)
        .on('name -> nameSearch, 'categoryId -> categoryId, 'parentId -> parentId.getOrElse(0L))
        .as(Dimension.simple *)
    }
  }

  def insert(name: String, categories: List[Long], description: Option[String], names: List[NameWithLanguage]) = {
    val now = new Date()
    DB.withConnection { implicit c =>
      // Create the dimension entry
      val dimensionId: Option[Long] =
        SQL"""
          INSERT INTO #$table(name, description, created_at)
          VALUES  ($name, $description, $now)
        """.executeInsert()

      // Add association to categories
      for(categoryId <- categories) {
        SQL"""
          INSERT INTO dimension_category (dimension_id, category_id)
          VALUES  ($dimensionId, $categoryId)
        """.executeInsert()
      }

      // Add names
      for(name <- NameWithLanguage(name, None) :: names) {
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
}