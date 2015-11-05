package controllers

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.jackson.ElasticJackson
import elasticsearch.ElasticSearch
import org.json4s.ext.EnumSerializer
import models._
import play.api.libs.json.Json
import play.api.mvc._
import queries.{SearchableItem, SearchResult, CreateQuery, CreateOption}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import ElasticJackson.Implicits._

object QueryProcessor {

  val itemCache = mutable.HashMap[SearchableItem[_], Future[SearchResult]]()
  def searchItem(item : SearchableItem[_]): Future[SearchResult] = {
//    itemCache.getOrElseUpdate(item, {
      if(item.createOption == CreateOption.Always) {
        createItem(item)
      } else {
        (for(itemId <- item.getId) yield {
          ElasticSearch.client.execute(get id itemId from item.getLocation).map(matchingItem => {
            if(matchingItem.isExists) {
              val json = Json.parse(matchingItem.getSourceAsString)
              SearchResult(Some(matchingItem.getId), Some(json), isCreated = false, 1, Seq(), idGivenButMissing = false)
            } else {
              SearchResult(None, None, isCreated = false, 1, Seq(), idGivenButMissing = true)
            }
          })
        }).getOrElse {
          findExactlyMatching(item).flatMap( optRes => {
            (for(res <- optRes) yield Future(res)).getOrElse {
              findMatching(item)
            }
          })
        }
      }
//    })
  }

  /**
   * Insert a new item
   * @return
   */
  def createItem(item : SearchableItem[_]): Future[SearchResult] = {
    item.indexQuery.flatMap { query =>
      val req = index into item.getLocation fields query
      ElasticSearch.client.execute { req }.map { res =>
        val source = Json.parse(req._fieldsAsXContent.string)
        SearchResult(Some(res.getId), Some(source), isCreated = true, nbHits = 0, Seq(), idGivenButMissing = false)
      }
    }
  }

  protected def findExactlyMatching(item : SearchableItem[_]): Future[Option[SearchResult]] = {
    item.exactMatchQuery.flatMap { termQuery =>
      val query = search in item.getLocation query { nestedQuery("names").query( bool { should { termQuery } } ) }
      ElasticSearch.client.execute { query }.map( res => {
        println(res)
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

  protected def findMatching(item : SearchableItem[_]): Future[SearchResult] = {
    item.matchQuery.flatMap { mq =>
      ElasticSearch.client.execute { search in item.getLocation query {
        bool { should(mq)}
      }}.flatMap(res => {
        Future(SearchResult(None, None, isCreated = false, 0, List(), idGivenButMissing = false))
        if(res.getHits.getTotalHits == 0 && item.createOption != CreateOption.Never) {
          // Create an entry if there are no hits and the create option is not never create
          createItem(item)
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

/**
 * Created by nico on 13/10/15.
 */
class Update extends Controller {
  import org.json4s._

  implicit val formats = DefaultFormats + new EnumSerializer(CreateOption)
  implicit val searchResultFormat = Json.writes[SearchResult]

  def update = Action.async { implicit rs =>
    val jsBody = rs.body.asJson.get.toString()
    val js = org.json4s.jackson.JsonMethods.parse(jsBody)
    val query = js.extract[CreateQuery]

    val queryItems = List("categories" -> query.categories, "dimensions" -> query.dimensions, "units" -> query.units, "oois" -> query.oois)
    val outJson = for((typeName, items) <- queryItems) yield {
      Future.sequence(for(item <- items) yield {
        QueryProcessor.searchItem(item).map( searchResult => {
          Json.toJson(searchResult)
        })

      }).map( json => Json.obj(typeName -> json))
    }
    val jsonForFacts = for(fact <- query.facts) yield {
      fact.searchItem().map( searchResult => { Json.toJson(searchResult) })
    }

    Future.sequence(outJson).map { responses => {
      var jsRes = Json.obj()
      for(r <- responses) {
        jsRes = jsRes ++ r
      }
      Ok(Json.toJson(jsRes))
    }}
  }


  def test = Action.async { implicit rs =>

    // Exact match query
    var foo = Map[String, List[String]]("en" -> List("Else", "Continent"), "fr" -> List("Continent"))

    val filteredQueries = for((lang, names) <- foo) yield {
      val filter = termsFilter(s"names.${lang}.raw", names:_*)
      filteredQuery filter(filter)
    }
//    val q = search in "nuata" / "category" query { bool { should { filteredQueries } must { termQuery("names.en.raw" -> "Continent") } } }
    val q = search in "nuata" / "category"
    /*
    val category = new Category(id = None, ref = "xx", names = Map("en" -> List("foo", "bar"), "fr" -> List()), descriptions = Map(), create = None)
    category.createItem().map( res => {
      println(res)
    })
    */

    ElasticSearch.client.execute { q }.map( res => {
      val resA = res.as[CategoryModel]
      for(c <- resA) {
        println(c.names)
      }
    })
    /*
    val jsBody = rs.body.asJson.get.toString()
    val js = org.json4s.jackson.JsonMethods.parse(jsBody)


    val foo = js.extract[Foo]
    println(foo.names)
    */
    Future.successful(Ok("Cool"))
  }
}
