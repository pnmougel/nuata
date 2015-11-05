package services

/**
 * Created by nico on 02/10/15.
 */

import play.api.libs.json._
import play.api.libs.json.Json._
import scalaj.http._
import oois.OOICreateQuery

/**
 * Created by nico on 01/10/15.
 */
object OOIs {
  case class OOI(id: Long, name: String, description: Option[String]) {}
  implicit val ooiFormat = Json.format[OOI]
  case class OOIResponse(created: Boolean, id: Option[Long], entry: Option[Long], entries: Option[List[OOI]])
  implicit val jsonQueryFormat = Json.format[OOIResponse]

  def create(query : OOICreateQuery): OOIResponse = {
    import oois.JsonFormat._

    val res = Http(s"${Config.serverUrl}/ooi").postData(Json.stringify(Json.toJson(query))).header("content-type", "application/json").asString
    println(res.body)
    Json.parse(res.body).as[OOIResponse]
  }
}
