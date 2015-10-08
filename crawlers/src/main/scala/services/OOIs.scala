package services

/**
 * Created by nico on 02/10/15.
 */

import play.api.libs.json._
import play.api.libs.json.Json._
import scalaj.http._

/**
 * Created by nico on 01/10/15.
 */
object OOIs {
  case class OOI(id: Long, name: String, description: Option[String]) {}
  implicit val ooiFormat = Json.format[OOI]
  case class OOIResponse(created: Boolean, id: Option[Long], entry: Option[OOI], entries: Option[List[OOI]])
  implicit val jsonQueryFormat = Json.format[OOIResponse]

  def create(name: String, unit: String, description: Option[String] = None, names: Array[String] = Array[String](), forceInsert: Boolean = false): OOIResponse = {
    val query = Json.obj(
      "name" -> name,
      "unit" -> unit,
      "description" -> description,
      "names" -> names
    )
    val res = Http(s"${Config.serverUrl}/ooi").postData(Json.stringify(query)).header("content-type", "application/json").asString
    Json.parse(res.body).as[OOIResponse]
  }
}
