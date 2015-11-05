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

  case class Res(id: Option[Long], isCreated: Boolean, matchingIds: List[Long])
  implicit val jsonResponseFormat = Json.format[Res]

  protected def doUpdate(path: String) = {
    val jsQuery = items.map( i => i.asJson)
    val dataQuery = Json.prettyPrint(Json.toJson(jsQuery))
    val res = Http(s"${Config.serverUrl}/$path")
      .postData(dataQuery)
      .header("content-type", "application/json").asString

    val generatedIds = Json.parse(res.body).as[List[Res]]

//    val response = parseResponse(res.body)
    for((item, generatedId) <- items.zip(generatedIds)) {
      if(generatedId.id.isDefined) {
        item.id = generatedId.id
      } else if(generatedId.matchingIds.length == 1) {
        item.id = Some(generatedId.matchingIds(0))
      }
    }
    items = List[ServerQuery]()
  }
}
