package elasticsearch

import com.sksamuel.elastic4s.ElasticClient

/**
 * Created by nico on 14/10/15.
 */
object ElasticSearch {
   val client = ElasticClient.remote("localhost", 9300)
//  val client = ElasticClient.local
}
