package services.caches

import scala.collection.mutable
import play.api.db._
import anorm._
import anorm.SqlParser._
import play.api.Play.current

/**
 * Created by nico on 07/10/15.
 *
 * A cache for all the names implemented as a string prefix tree to save memory and get fast prefix lookup
 */
//case class NameEntry(id: Long)
case class NameNode(children: mutable.HashMap[Char, NameNode], var entries: List[Long])
case class NameDescription(id: Long, nameSearch: String, languageId: Option[Long])

object NameCache {
  var cache = mutable.HashMap[(String, String), NameNode]()
  val languageIdToCode = mutable.HashMap[Long, String]()

  def buildLanguageMappings() = DB.withConnection { implicit c =>
    val languages = SQL"""SELECT id, code FROM Language""".as(long("id") ~ str("code") *)
    for(language <- languages) {
      languageIdToCode(language._1) = language._2
    }
    println(languageIdToCode.values.mkString("\n"))
  }

  implicit val nameEntryParser = {
    get[Long]("id") ~ get[String]("name_search") ~ get[Option[Long]]("language_id") map {
      case id ~ nameSearch ~ languageId  => NameDescription(id, nameSearch, languageId)
    }
  }

  private def insertNameInCache(name: String, idx: Int, id: Long, curNode: NameNode) : Unit = {
    if(idx >= name.size) {
      curNode.entries = id :: curNode.entries
    } else {
      val curChar = name.charAt(idx)
      val nextNode = curNode.children.getOrElseUpdate(curChar, NameNode(mutable.HashMap[Char, NameNode](), List[Long]()))
      insertNameInCache(name, idx + 1, id, nextNode)
    }
  }

  private def buildCacheForTable(kind: String, newCache: mutable.HashMap[(String, String), NameNode]) = DB.withConnection { implicit c =>
    val query = s"SELECT id, name_search, language_id, ${kind + "_id"} FROM ${kind + "_name"}"
    val names = SQL(query).as(nameEntryParser *)
    for(name <- names) {
      val language = languageIdToCode.getOrElse(name.languageId.getOrElse(-1L), "")
      val curNode = newCache.getOrElseUpdate((language, kind), NameNode(mutable.HashMap[Char, NameNode](), List[Long]()))
      insertNameInCache(name.nameSearch.trim, 0, name.id, curNode)
    }
  }

  def buildCache() = {
    cache.clear()
    val newCache = mutable.HashMap[(String, String), NameNode]()
    buildLanguageMappings()
    for(tableName <- List("ooi", "dimension")) {
      buildCacheForTable(tableName, newCache)
    }
    cache = newCache
  }

  def findName(name: String, languageCode: String, kind: String, idx: Int) : List[Long] = {
    val nameSizeBound = name.size - 1
    var curIdx = idx - 1
    var curNode = cache((languageCode, kind))
    var hasNext = true
    var curMatch = ""
    while(hasNext && curIdx != nameSizeBound) {
      curIdx += 1
      val curChar = name.charAt(curIdx)
      curMatch = curMatch + curChar
      hasNext = curNode.children.contains(curChar)
      if(hasNext) {
        curNode = curNode.children(curChar)
      }
    }
    curNode.entries
  }

  def findNames(query: String, languageCode: String, kind: String) : List[Long] = {
    var ids = List[Long]()
    for((c, i) <- query.zipWithIndex) {
      if(i == 0 || c == ' ') {
        val idx = if(i == 0) 0 else i + 1
        ids = findName(query, languageCode, kind, idx) ::: ids
      }
    }
    ids
  }

  def findDimensions(query: String, languageCode: String) = findNames(query, languageCode, "dimension")

  def findOOIs(query: String, languageCode: String) = findNames(query, languageCode, "ooi")
}
