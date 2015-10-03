package categories

import java.util.Date

import languages.{NameWithLanguage, Language}
import play.api.db._
import anorm._
import anorm.SqlParser._
import play.api.Play.current

/**
 * Created by nico on 01/10/15.
 *
 * Model for the categories
 */
case class Category(id: Long, name: String, description: Option[String]) {}

object Category {
  val simple = {
    get[Long]("id") ~ get[String]("name") ~ get[Option[String]]("description") map {
      case id ~ name ~ description  => Category(id, name, description)
    }
  }
  val tableName = "category"

  def findAll(): Seq[Category] = {
    DB.withConnection { implicit c =>
      SQL"SELECT * FROM #$tableName".as(Category.simple *)
    }
  }

  def findByName(name: String) : List[Category] = {
    DB.withConnection { implicit c =>
      SQL"""
        SELECT * FROM #$tableName
        WHERE category.id IN
          (SELECT DISTINCT(category_id)
          FROM category_name
          WHERE name = $name)
      """.as(Category.simple *)
    }
  }


  def findIdByName(name: String) : List[Long] = {
    val nameLower = name.toLowerCase
    DB.withConnection { implicit c =>
      SQL"""
        SELECT DISTINCT(category_id)
        FROM category_name
        WHERE name_search = $nameLower
      """.as(SqlParser.long("id") *)
    }
  }

  def insert(name: String, description: Option[String], names: List[NameWithLanguage]) : Option[Long] = {
    val now = new Date()
    DB.withConnection { implicit c =>
      val categoryId: Option[Long] = SQL"""INSERT INTO #$tableName(name, description, created_at) VALUES  ($name, $description, $now)""".executeInsert()
      for(name <- NameWithLanguage(name, None) :: names) {
        val languageId = for(languageCode <- name.language) yield { Language.getOrCreate(languageCode) }
        SQL"""
          INSERT INTO category_name
          (name, name_search, language_id, category_id)
          VALUES (${name.name}, ${name.nameSearch}, ${languageId}, $categoryId)
        """.executeInsert()
      }
      categoryId
    }
  }
}