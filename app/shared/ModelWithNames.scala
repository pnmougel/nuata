package shared

import anorm.SqlParser._
import anorm._
import languages.{NameWithLanguage, Language}
import play.api.db.DB
import play.api.Play.current
import services.caches.NameCache

/**
 * Created by nico on 07/10/15.
 */
class ModelWithNames(tableName: String) {

  def insertNames(names: Seq[NameWithLanguage], id: Long, tableName: String)(implicit c: java.sql.Connection) = {
    for(name <- names) {
      NameCache.insertName(name.name, id, name.lang, tableName)

      val languageId = Language.getOrCreate(name.lang)
      SQL(s"""
           INSERT INTO ${tableName}_name
           (name, name_search, language_id, ${tableName}_id)
           VALUES ({name}, {nameSearch}, {languageId}, {id})
         """)
        .on("name" -> name.name, "nameSearch" -> name.nameSearch, "languageId" -> languageId, "id" -> id)
        .executeInsert()
    }
  }

  def findByNames(names: Seq[(String, Long)]) : List[Long] = {
    DB.withConnection { implicit c =>
      val queryNameSearch = if(names.isEmpty) "" else names.map(name => {
        s"(name_search = '${name._1.toLowerCase}' AND language_id = ${name._2})"
      }).mkString(" WHERE ", " OR ", "")

      val q = s"""
        SELECT DISTINCT(${tableName}_id)
        FROM ${tableName}_name
        $queryNameSearch"""
      println(q)
      SQL(q).as(long(s"${tableName}_id") *)
    }
  }
}
