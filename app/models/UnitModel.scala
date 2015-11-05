package models

import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by nico on 02/11/15.
 */
case class UnitModel(_id: Option[String], _score: Option[Double], names: Map[String, List[String]], descriptions: Map[String, String])
  extends LocalizedNamedModel(_id, _score, names, descriptions) {

  override def getIndexQuery() = defaultIndexQuery
  override def getSearchQuery() = defaultSearchQuery
  override def getMatchQuery() = defaultMatchQuery


  def toJson : Future[JsObject] = {
    Future(Json.obj(
      "names" -> names,
      "descriptions" -> descriptions
    ))
  }
}
