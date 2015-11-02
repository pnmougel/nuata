package models

/**
 * Created by nico on 14/10/15.
 *
 */

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import elasticsearch.ElasticSearch
import play.api.libs.json.{Json, JsValue}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class SearchableItem(
          location: (String, String),
          id: Option[String],
          names: Map[String, List[String]],
          descriptions: Map[String, List[String]],
          create: Option[String],
          defaultCreateOption: CreateOption.Value)
  extends ItemWithDependencies {

  def buildRefMapping(createQuery: CreateQuery): Unit

  var createOption: CreateOption.Value = defaultCreateOption

  def initCreateOption(createQuery: CreateQuery) = {
    val curDefaultCreateOption = createQuery.createOption.getOrElse(defaultCreateOption)
    createOption = CreateOption.fromString(create).getOrElse(curDefaultCreateOption)
  }

  /**
   * Override this method to change the content of the inserted document
   * @return
   */
  def insertQuery = Future(defaultInsertQuery)

  def getMatchQuery: Future[List[MatchQueryDefinition]] = Future(defaultMatchQueries)

  def getTermQuery: Future[List[QueryDefinition]] = Future(defaultTermQueries)

//  var defaultTermQueries = List[TermQueryDefinition]()
  var defaultMatchQueries = List[MatchQueryDefinition]()

  val defaultTermQueries = (for((lang, localizedName) <- names) yield {
    filteredQuery filter termsFilter(s"names.${lang}.raw", localizedName:_*)
  }).toList

  // Create a base insert query using only the names and descriptions
  var defaultInsertQuery = mutable.HashMap[String, Any]()
  defaultInsertQuery("names") = names
  defaultInsertQuery("descriptions") = descriptions

  /*
  for(name <- allNames) {
    val key = s"names_${name.lang.name}"
    val keyRaw = key + ".raw"
//    defaultTermQueries = termQuery(keyRaw, name.name) :: defaultTermQueries
    defaultMatchQueries = matchQuery(key, name.name) :: defaultMatchQueries
    defaultInsertQuery(key) = name.name :: defaultInsertQuery.getOrElse(key, List[String]())
  }

  for(description <- allDescriptions) {
    val key = s"descriptions_${description.lang.name}"
    if(createOption != CreateOption.IfNameNotMatching) {
      defaultMatchQueries = matchQuery(key, description.description) :: defaultMatchQueries
    }
    defaultInsertQuery(key) = description.description :: defaultInsertQuery.getOrElse(key, List[String]())
  }
  */

  val client = ElasticSearch.client

  var searchResult: Option[Future[SearchResult]] = None

  def searchItem(): Future[SearchResult] = {
    searchResult.getOrElse({
      val res = if(createOption == CreateOption.Always) {
        createItem()
      } else {
        (for(itemId <- id) yield {
          client.execute(get id itemId from location)
            .map(matchingItem => {
              if(matchingItem.isExists) {
                val json = Json.parse(matchingItem.getSourceAsString)
                SearchResult(Some(matchingItem.getId), Some(json), isCreated = false, 1, Seq(), idGivenButMissing = false)
              } else {
                SearchResult(None, None, isCreated = false, 1, Seq(), idGivenButMissing = true)
              }
            })
        }).getOrElse {
          findExactlyMatching().flatMap( optRes => {
            (for(res <- optRes) yield Future(res)).getOrElse {
              findMatching()
            }
          })
        }
      }
      searchResult = Some(res)
      res
    })
  }

  /**
   * Insert a new item
   * @return
   */
  def createItem(): Future[SearchResult] = {
    insertQuery.flatMap { query =>
      val req = index into location fields query
      client.execute { req }.map { res =>
        val source = Json.parse(req._fieldsAsXContent.string)
        SearchResult(Some(res.getId), Some(source), isCreated = true, nbHits = 0, Seq(), idGivenButMissing = false)
      }
    }
  }

  protected def findExactlyMatching(): Future[Option[SearchResult]] = {
    getTermQuery.flatMap { tq =>
      client.execute { search in location query { bool { should (tq)} }}.map( res => {
        if(res.getHits.getHits.length == 1) {
          val hit = res.getHits.getHits()(0)
          val json = Json.parse(hit.getSourceAsString)
          Some(SearchResult(Some(hit.getId), Some(json), isCreated = false, 1, Seq(), idGivenButMissing = false))
        } else {
          None
        }
      })
    }
  }

  protected def findMatching(): Future[SearchResult] = {
    getMatchQuery.flatMap { mq =>
      client.execute { search in location query {
        bool { should(mq)}
      }}.flatMap(res => {
        Future(SearchResult(None, None, isCreated = false, 0, List(), idGivenButMissing = false))
        if(res.getHits.getTotalHits == 0 && createOption != CreateOption.Never) {
          // Create an entry if there are no hits and the create option is not never create
          val res = createItem()
          res
        } else {
          // Otherwise returns the hits
          val hits = res.getHits.getHits()
          val json = hits.map( hit => {
            Json.obj(
              "id" -> hit.id(),
              "score" -> hit.score(),
              "source" -> Json.parse(hit.getSourceAsString))
          })
          Future(SearchResult(None, None, isCreated = false, res.totalHits, json, idGivenButMissing = false))
        }
      })
    }
  }
}
