package repositories.commons

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.QueryDefinition
import elasticsearch.ElasticSearch
import models.LocalizedNamedModel
import org.elasticsearch.action.search.SearchResponse
import org.json4s._
import repositories.BaseRepository
import shared.Languages

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by nico on 02/11/15.
 */
abstract class LocalizedNamedItemRepository[T](`type`: String)
  extends BaseRepository(`type`) {
  /**
   * Convert a search response to an array of model instance
   * @param res
   * @return
   */
  def resultToEntity(res: SearchResponse) : Array[T]

  def byId(id: String): Future[T] = {
    ElasticSearch.client.execute {get id id from path}.map(item => {
      fromJsonStr(item.getSourceAsString)
    })
  }

  def byIdOpt(id: String): Future[Option[T]] = {
    ElasticSearch.client.execute {get id id from path}.map(item => {
      if(item.isExists) {
        Some(fromJsonStr(item.getSourceAsString))
      } else {
        None
      }
    })
  }

  def nameContaining(query: String): Future[SearchResponse] = {
    val langQueries = Languages.available.map( lang => { matchQuery(lang, query) })
    val esQuery = search in path query nestedQuery("names").query( bool { should { langQueries } } )
    ElasticSearch.client.execute { esQuery }
  }

  def nameStartingWith(word: String) = {
    val langQueries = Languages.available.map( lang => { prefixQuery(lang, word) })
    val esQuery = search in path query nestedQuery("names").query( bool { should { langQueries } } )
    ElasticSearch.client.execute { esQuery }
  }

  def doFilteredQuery(query: Future[List[QueryDefinition]]): Future[Array[T]] = {
    query.flatMap( q => {
      ElasticSearch.client.execute(search in path query { bool { should (q)} }).map( res => {
        resultToEntity(res)
      })
    })
  }

  protected def jsToInstance(jValue: JValue): T

  def fromJsonStr(jsonStr: String): T = {
    jsToInstance(org.json4s.jackson.JsonMethods.parse(jsonStr))
  }

//  def indexItem(model: LocalizedNamedModel) : Future[String] = {
//    client.execute(index into path fields model.getIndexQuery).map( result => result.getId)
//  }

  def indexItems(models: List[LocalizedNamedModel]) : Future[Array[String]] = {
    val indexQueries = models.map( model => index into path fields model.getIndexQuery)
    client.execute(bulk (indexQueries) ).map( results => {
      results.getItems.map( _.getId )
    })
  }

//  def searchExact(model: LocalizedNamedModel): Future[Array[T]] = {
//    // filteredQuery filter
//    client.execute(search in path query {
//      nestedQuery("names").query( bool { should { model.getSearchQuery } } )
//    }).map( res => { resultToEntity(res) })
//  }

  def searchExacts(models: List[LocalizedNamedModel]): Future[mutable.ArraySeq[Array[T]]] = {
    val queries = models.map(model => {
      search in path query {
        nestedQuery("names").query( bool {
          model.getSearchQuery
        })
      }})
    client.execute( multi (queries)).map(res => {
      res.getResponses.map { x =>
        resultToEntity(x.getResponse)
      }
    })
  }

//  def searchMatch(model: LocalizedNamedModel): Future[Array[T]] = {
//    client.execute(search in path query { bool { should( model.getMatchQuery )}
//    }).map(res => { resultToEntity(res) })
//  }

  def searchMatches(models: List[LocalizedNamedModel]): Future[mutable.ArraySeq[Array[T]]] = {
    val queries = models.map(model => search in path query { bool { should( model.getMatchQuery )} })

    client.execute( multi (queries)).map(res => {
      res.getResponses.map( x => resultToEntity(x.getResponse))
    })
  }
}
