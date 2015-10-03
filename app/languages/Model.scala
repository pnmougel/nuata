package languages

import anorm._
import play.api.Play.current
import play.api.db._

import scala.collection.mutable

/**
 * Created by nico on 01/10/15.
 *
 */
case class Language(id: Long, code: String) {}

case class NameWithLanguage(name: String, language: Option[String]) {
  lazy val nameSearch = name.trim().toLowerCase
}

object Language {
  val table = "Language"

  // Language cache
  val languageCache = mutable.HashMap[String, Long]()

  def getOrCreate(code: String) : Long = {
    languageCache.getOrElseUpdate(code, {
      DB.withConnection { implicit c =>
        val languageId = SQL"""SELECT id FROM #$table WHERE code = $code""".as(SqlParser.long("id").singleOpt)
        languageId.getOrElse {
          val z : Option[Long] = SQL"""INSERT INTO #$table (code) VALUES $code""".executeInsert()
          z.getOrElse(0L)
        }
      }
    })
  }

  /*
  val simple = {
    get[Long]("id") ~ get[String]("code") map {
      case id ~ code => Language(id, code)
    }
  }
  */
}