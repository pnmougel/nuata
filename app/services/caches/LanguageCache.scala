package services.caches

import anorm.SqlParser._
import play.api.db.DB
import play.api.db._
import anorm._
import anorm.SqlParser._
import play.api.Play.current

import scala.collection.mutable

/**
 * Created by nico on 07/10/15.
 */
object LanguageCache {
  val cache = mutable.HashMap[Long, String]()

  def build() = DB.withConnection { implicit c =>
    val languages = SQL"""SELECT id, code FROM Language""".as(long("id") ~ str("code") *)
    for(language <- languages) {
      cache(language._1) = language._2
    }
  }
}
