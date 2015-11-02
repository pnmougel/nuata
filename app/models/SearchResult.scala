package models

import play.api.libs.json.JsValue

/**
 * Created by nico on 14/10/15.
 */
case class SearchResult(
                 id: Option[String],
                 source: Option[JsValue],
                 isCreated: Boolean,
                 nbHits: Long,
                 hits: Seq[JsValue],
                 idGivenButMissing: Boolean) {

}


