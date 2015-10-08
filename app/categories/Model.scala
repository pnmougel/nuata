package categories

import java.util.Date

import languages.{NameWithLanguage, Language}
import play.api.db._
import anorm._
import anorm.SqlParser._
import play.api.Play.current
import services.caches.NameCache
import shared.ModelWithNames

/**
 * Created by nico on 01/10/15.
 *
 * Model for the categories
 */
case class Category(id: Long, description: Option[String]) {}

object Category extends ModelWithNames("category") {
  val simple = {
    get[Long]("id") ~ get[Option[String]]("description") map {
      case id ~ description  => Category(id, description)
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

//
//  def findByNames(names: Seq[(String, Long)]) : List[Long] = {
//    DB.withConnection { implicit c =>
//      val queryNameSearch = names.map(name => {
//        s"(name_search = '${name._1}' AND language_id = ${name._2})"
//      }).mkString(" OR ")
//      SQL(s"""
//        SELECT DISTINCT(category_id)
//        FROM category_name
//        WHERE $queryNameSearch
//      """).as(long("category_id") *)
//    }
//  }

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

  def batchInsert2(queries: List[CategoryQuery])(implicit c : java.sql.Connection) : List[Long] = {
    val now = new Date()
      for(query <- queries) yield {
        val categoryId =
          SQL"""
               INSERT INTO #$tableName(description, created_at)
               VALUES (${query.description}, $now)"""
            .executeInsert().asInstanceOf[Option[Long]].get
        insertNames(query.names, categoryId, "category")
        //        for(name <- query.names) {
        //          NameCache.insertName(name.name, categoryId.get, name.lang, "category")
        //
        //          val languageId = Language.getOrCreate(name.lang)
        //          SQL"""
        //          INSERT INTO category_name
        //          (name, name_search, language_id, category_id)
        //          VALUES (${name.name}, ${name.nameSearch}, ${languageId}, $categoryId)
        //        """.executeInsert()
        //        }
        categoryId
      }
  }

  def batchInsert(queries: List[CategoryQuery]) : List[Long] = {
    val now = new Date()
    DB.withTransaction { implicit c =>
      for(query <- queries) yield {
        val categoryId =
          SQL"""
               INSERT INTO #$tableName (description, created_at)
               VALUES (${query.description}, $now)"""
            .executeInsert().asInstanceOf[Option[Long]].get
        insertNames(query.names, categoryId, "category")
//        for(name <- query.names) {
//          NameCache.insertName(name.name, categoryId.get, name.lang, "category")
//
//          val languageId = Language.getOrCreate(name.lang)
//          SQL"""
//          INSERT INTO category_name
//          (name, name_search, language_id, category_id)
//          VALUES (${name.name}, ${name.nameSearch}, ${languageId}, $categoryId)
//        """.executeInsert()
//        }
        categoryId
      }
    }
  }
}