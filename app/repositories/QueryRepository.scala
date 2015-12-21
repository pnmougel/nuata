package repositories

import java.util.Date

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.jackson.ElasticJackson.Implicits._
import elasticsearch.ElasticSearch
import models.DimensionModel
import org.elasticsearch.action.search.SearchResponse
import org.json4s._
import repositories.commons.LocalizedNamedItemRepository

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by nico on 02/11/15.
 */
object QueryRepository  {
  implicit val formats = DefaultFormats

  def logQuery(query: String, dimensionIds: List[String], categoryIds: List[String], unitIds: List[String], ooiIds: List[String], ip: String) = {
    val log = Map("query" -> query,
      "dimensionIds" -> dimensionIds,
      "categoryIds" -> categoryIds,
      "unitIds" -> unitIds,
      "ooiIds" -> ooiIds,
      "ip" -> ip,
      "at" -> new Date())
    ElasticSearch.client.execute{
      index into "nuata" / "query" fields log
    }
  }
}