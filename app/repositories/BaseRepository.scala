package repositories

import com.sksamuel.elastic4s.ElasticDsl._
import elasticsearch.ElasticSearch
import play.api.libs.json.{Json, JsValue}

import scala.concurrent.Future

/**
 * Created by nico on 02/11/15.
 */
class BaseRepository(`type`: String) {
  val path = "nuata" / `type`
  val client = ElasticSearch.client

  def count = {
    ElasticSearch.client.execute { com.sksamuel.elastic4s.ElasticDsl.count from "nuata" types `type` }
  }

}
