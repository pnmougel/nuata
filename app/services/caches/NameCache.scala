package services.caches

import scala.collection.mutable
import play.api.db._
import anorm._
import anorm.SqlParser._
import play.api.Play.current


/**
 * A cache for all the names implemented as a prefix tree to save memory and get fast prefix lookup
 */
object NameCache {
  /**
   * Node of the prefix tree
   * @param children list of children nodes
   * @param entries entries found at the node
   */
  case class NameNode(children: mutable.HashMap[Char, NameNode], var entries: List[Long])

  var cache = mutable.HashMap[(String, String), NameNode]()

  implicit val nameEntryParser = {
    get[Long]("id") ~ get[String]("name_search") ~ get[Option[Long]]("language_id") map {
      case id ~ nameSearch ~ languageId  => (id, nameSearch, languageId)
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
    for((id, nameSearch, languageId) <- names) {
      val language = LanguageCache.cache.getOrElse(languageId.getOrElse(-1L), "")
      val curNode = newCache.getOrElseUpdate((language, kind), NameNode(mutable.HashMap[Char, NameNode](), List[Long]()))
      insertNameInCache(nameSearch.trim, 0, id, curNode)
    }
  }

  /**
   * Rebuild the cache
   */
  def build() = {
    cache.clear()
    val newCache = mutable.HashMap[(String, String), NameNode]()
    for(tableName <- List("ooi", "dimension", "category")) {
      buildCacheForTable(tableName, newCache)
    }
    cache = newCache
    println("Cache built")
    println(cache(("", "category")).children.size)
  }

  private def findName(name: String, languageCode: String, kind: String, idx: Int) : List[Long] = {
    val nameSizeBound = name.size - 1
    var curIdx = idx - 1
    if(cache.contains((languageCode, kind))) {
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
    } else {
      List[Long]()
    }
  }

  private def findNames(query: String, languageCode: String, kind: String) : List[Long] = {
    var ids = List[Long]()
    for((c, i) <- query.zipWithIndex) {
      if(i == 0 || c == ' ') {
        val idx = if(i == 0) 0 else i + 1
        ids = findName(query, languageCode, kind, idx) ::: ids
      }
    }
    ids
  }

  /**
   * Add an entry to the name cache
   * @param name
   * @param id
   * @param language
   * @param kind
   */
  def insertName(name: String, id: Long, language: String, kind: String) = {
    val curNode = cache.getOrElseUpdate((language, kind), NameNode(mutable.HashMap[Char, NameNode](), List[Long]()))
    insertNameInCache(name.trim, 0, id, curNode)
  }

  /**
   * Find dimension ids matching a query for a language code
   * @param query
   * @param languageCode
   * @return
   */
  def findDimensions(query: String, languageCode: String) = findNames(query, languageCode, "dimension")

  /**
   * Find object of interest ids matching a query for a language code
   * @param query
   * @param languageCode
   * @return
   */
  def findOOIs(query: String, languageCode: String) = findNames(query, languageCode, "ooi")


  /**
   * Find category ids matching a query for a language code
   * @param query
   * @param languageCode
   * @return
   */
  def findCategories(query: String, languageCode: String): List[Long] = findNames(query, languageCode, "category")
}
