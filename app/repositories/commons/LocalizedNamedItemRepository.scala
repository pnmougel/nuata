package repositories.commons

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.{ElasticDsl, QueryDefinition}
import elasticsearch.ElasticSearch
import models.LocalizedNamedModel
import org.elasticsearch.action.count.CountResponse
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.search.SearchResponse
import org.json4s._
import repositories.{SearchOptions, NameOperations, BaseRepository}
import repositories.NameOperations.NameOperations
import shared.Languages

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
/**
 * Created by nico on 02/11/15.
 */
abstract class LocalizedNamedItemRepository[T](val `type`: String)
  extends BaseRepository(`type`) {
  /**
   * Convert a search response to an array of model instance
   * @param res
   * @return
   */
  def resultToEntity(res: SearchResponse) : Array[T]

  def resultToEntity(res: GetResponse): T = {
//    val js = org.json4s.jackson.JsonMethods.parse(res.getSourceAsString) merge (("_id" -> res.getId) ~ ("_id" -> res.getId))
//    jsToInstance(js)
    val js = org.json4s.jackson.JsonMethods.parse(res.getSourceAsString).asInstanceOf[JObject]
    jsToInstance(js ~ ("_id" -> res.getId))
  }


  protected def jsToInstance(jValue: JValue): T

  def byId(id: String): Future[T] = {
    ElasticSearch.client.execute {get id id from path}.map(resultToEntity)
  }

  def byIdOpt(id: String): Future[Option[T]] = {
    ElasticSearch.client.execute {get id id from path}.map(item => {
      if(item.isExists) Some(resultToEntity(item)) else None
    })
  }

  def nameMatchQuery(name: String) : QueryDefinition = {
    nestedQuery("otherNames").query( bool {
      should { for(lang <- Languages.available) yield {
        matchQuery(s"otherNames.$lang", name)
      }}
    })
  }

  def nameTermQuery(name: String) : QueryDefinition = {
    nestedQuery("otherNames").query( bool {
      should { for(lang <- Languages.available) yield {
        filteredQuery filter termFilter(s"otherNames.$lang.raw", name)
      }}
    })
  }

  def nameStartsWithQuery(name: String) : QueryDefinition = {
    nestedQuery("otherNames").query( bool {
      should { for(lang <- Languages.available) yield {
        filteredQuery filter prefixFilter(s"otherNames.$lang.raw", name.toLowerCase)
      }}
    })
  }

  def doSearch(searchOptions: SearchOptions) : Future[(CountResponse, SearchResponse)] = {
    val nameQuery = searchOptions.nameOperation match {
      case NameOperations.StartsWith => nameStartsWithQuery(searchOptions.name)
      case NameOperations.Match => nameMatchQuery(searchOptions.name)
      case NameOperations.Exact => nameTermQuery(searchOptions.name)
      case _ => nameStartsWithQuery(searchOptions.name)
    }
    val idsQuery = for((field, itemIds) <- searchOptions.filters.toList) yield {
      must(itemIds.map(id => termQuery(field, id)))
    }

    val filterQuery = bool { must { nameQuery :: idsQuery } }
    val countQuery = ElasticDsl.count.from(path).where(filterQuery)
    val searchQuery = search in path query filterQuery

    ElasticSearch.client.execute(countQuery).flatMap( countRes => {
      ElasticSearch.client.execute { searchQuery start searchOptions.start limit searchOptions.limit }.map(items => {
        (countRes, items)
      })
    })
  }



  def join(fieldName: String, ids: List[String]) = {
    val query = search in path query { termsQuery(fieldName, ids :_*) }
    ElasticSearch.client.execute(query limit 10000)
  }

  def doSearchWithMapping(searchOptions: SearchOptions) = {
    doSearch(searchOptions).map( res => {
      val (count, search) = res
      Map("nbItems" -> count.getCount,
        "items" -> resultToEntity(search))
    })
  }

  def searchAndExpand(searchOptions: SearchOptions) = {
    doSearch(searchOptions).flatMap( res => {
      val (count, search) = res
      Future.sequence(resultToEntity(search).toList.map( item => {
        item.asInstanceOf[LocalizedNamedModel].toJson
      })).map( jValues => {
        Map("nbItems" -> count.getCount,
            "items" -> jValues)
      })
    })
  }

  def nameContaining(query: String): Future[SearchResponse] = {
    val langQueries = Languages.available.map( lang => { matchQuery(lang, query) })
    val esQuery = search in path query nestedQuery("otherNames").query( bool { should { langQueries } } )
    ElasticSearch.client.execute { esQuery }
  }


  def doFilteredQuery(query: Future[List[QueryDefinition]]): Future[Array[T]] = {
    query.flatMap( q => {
      ElasticSearch.client.execute(search in path query { bool { should (q)} }).map( res => {
        resultToEntity(res)
      })
    })
  }

  def indexItems(models: List[LocalizedNamedModel]) : Future[Array[String]] = {


    val indexQueries = models.map( model => index into path fields model.getIndexQuery)
    client.execute(bulk (indexQueries) ).map( results => {
      results.getItems.map( _.getId )
    })
  }

  def searchExacts(models: List[LocalizedNamedModel]): Future[mutable.ArraySeq[Array[T]]] = {
    val queries = models.map(model => {
      search in path query {
        nestedQuery("otherNames").query( bool {
          model.getSearchQuery
        })
      }})
    client.execute( multi (queries)).map(res => {
      res.getResponses.map { x =>
        resultToEntity(x.getResponse)
      }
    })
  }

  def searchMatches(models: List[LocalizedNamedModel]): Future[mutable.ArraySeq[Array[T]]] = {
    val queries = models.map(model => search in path query { bool { should( model.getMatchQuery )} })

    client.execute( multi (queries)).map(res => {
      res.getResponses.map( x => resultToEntity(x.getResponse))
    })
  }
}
