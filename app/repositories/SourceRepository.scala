package repositories

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.jackson.ElasticJackson.Implicits._
import models.{SourceModel, FactModel}
import org.elasticsearch.action.search.SearchResponse
import org.json4s._
import repositories.commons.LocalizedNamedItemRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by nico on 02/11/15.
 */

object SourceRepository extends LocalizedNamedItemRepository[SourceModel]("source") {
  implicit val formats = DefaultFormats

  def resultToEntity(res: SearchResponse) = res.as[SourceModel]

  protected def jsToInstance(jValue: JValue) = jValue.extract[SourceModel]

  def indexSources(sources: List[SourceModel]): Future[Array[String]] = {
    val indexQueries = sources.map( source => {
      var indexQuery = Map[String, Any](
        "name" -> source.name, "kind" -> source.kind, "authors" -> source.authors)
      for(v <- source.url) { indexQuery += ("url" -> v) }
      index into path fields indexQuery
    })
    client.execute(bulk (indexQueries) ).map( results => {
      results.getItems.map( _.getId )
    })
  }
}
