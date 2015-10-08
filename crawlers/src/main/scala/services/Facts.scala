package services

import play.api.libs.json.{JsValue, Json}

import scalaj.http.Http

/**
 * Created by nico on 02/10/15.
 */
object Facts {
  def create(value: Long, dimensions: List[Long], ooi: Long) = {
    val query = Json.obj(
      "value" -> value,
      "dimensions" -> dimensions,
      "ooi" -> ooi
    )
    sendQuery(query)
  }

  def create(value: Double, dimensions: List[Long], ooi: Long) = {
    val query = Json.obj(
      "value" -> value,
      "dimensions" -> dimensions,
      "ooi" -> ooi
    )
    sendQuery(query)
  }

  def sendQuery(query: JsValue) = {
    val data = Json.stringify(query)
    val res = Http(s"${Config.serverUrl}/fact")
      .postData(data)
      .header("content-type", "application/json")
      .asString
  }
}
