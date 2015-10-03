package oois

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
case class OOI(id: Long, name: String, description: Option[String]) {}

object OOI {
  val simple = {
    get[Long]("id") ~ get[String]("name") ~ get[Option[String]]("description") map {
      case id ~ name ~ description  => OOI(id, name, description)
    }
  }
  val tableName = "ooi"

  def findAll(): Seq[OOI] = {
    DB.withConnection { implicit c =>
      SQL"SELECT * FROM #$tableName".as(simple *)
    }
  }

  def findByName(name: String) : List[OOI] = {
    val nameLower = name.toLowerCase
    DB.withConnection { implicit c =>
      SQL"""
        SELECT * FROM #$tableName
        WHERE id IN
          (SELECT DISTINCT(ooi_id)
          FROM ooi_name
          WHERE name_search = $nameLower)
      """.as(simple *)
    }
  }

  def findIdByName(name: String) : List[Long] = {
    val nameLower = name.toLowerCase
    DB.withConnection { implicit c =>
      SQL"""
        SELECT DISTINCT(ooi_id)
        FROM ooi_name
        WHERE name_search = $nameLower
      """.as(SqlParser.long("id") *)
    }
  }

  def insert(name: String, unit: String, description: Option[String], names: List[NameWithLanguage]) : Option[Long] = {
    val now = new Date()
    DB.withConnection { implicit c =>
      // Get the corresponding unit id
      val unitId = units.Unit.getOrCreate(unit)

      // Save the languages
      val ooiId: Option[Long] = SQL"""
             INSERT INTO #$tableName (name, description, created_at, unit_id)
             VALUES  ($name, $description, $now, $unitId)
             """.executeInsert()
      for(name <- NameWithLanguage(name, None) :: names) {
        val languageId = for(languageCode <- name.language) yield { Language.getOrCreate(languageCode) }
        SQL"""
          INSERT INTO ooi_name
          (name, name_search, language_id, ooi_id)
          VALUES (${name.name}, ${name.nameSearch}, ${languageId}, $ooiId)
        """.executeInsert()
      }
      ooiId
    }
  }
}