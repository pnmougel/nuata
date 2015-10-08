package services

import play.api.libs.json.{JsObject, JsValue, Json}

import scalaj.http.Http

/**
 * Created by nico on 08/10/15.
 */

abstract class ServerQuery(var id: Option[Long] = None) {
  def asJson : JsValue
}

abstract class ServerResponse(var id: Option[Long] = None)

abstract class ServerConnection {
  var items = List[ServerQuery]()

  def parseResponse(body: String) : List[ServerResponse]

  protected def doUpdate(path: String) = {
    val jsQuery = items.map( i => i.asJson)
    val dataQuery = Json.prettyPrint(Json.toJson(jsQuery))
    val res = Http(s"${Config.serverUrl}/$path")
      .postData(dataQuery)
      .header("content-type", "application/json").asString
    val response = parseResponse(res.body)
    for((item, res) <- items.zip(response)) {
      item.id = res.id
    }
    items = List[ServerQuery]()
  }
}
