package models

import org.json4s.Extraction
import org.json4s.JsonAST.JObject
import play.api.libs.json.{JsObject, Json}
import repositories.SourceRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import org.json4s._
import org.json4s.JsonAST._
import org.json4s.JsonDSL._

/**
 * Created by nico on 02/11/15.
 */
case class UnitModel(_id: Option[String],
                     _score: Option[Double],
                     kind: String,
                     name: Map[String, String],
                     otherNames: Map[String, List[String]],
                     description: Map[String, String],
                     sourceIds: List[String],
                     meta: Map[String, _])
  extends LocalizedNamedModel(_id, _score, name, otherNames, description, meta)
  with JsonSerializable {

  implicit val formats = DefaultFormats

  lazy val sources = Future.sequence(for(sourceId <- sourceIds) yield { SourceRepository.byId(sourceId) })

  override def getIndexQuery() = {
    defaultIndexQuery ++ Map("sourceIds" -> sourceIds) ++ Map("kind" -> kind)
  }
  override def getSearchQuery() = defaultSearchQuery
  override def getMatchQuery() = defaultMatchQuery

  def toJson : Future[JObject] = {
    for(sourcesList <- toJsonSeq(sources)) yield {
      ("_id" -> _id) ~
        ("_score" -> _score) ~
        ("kind" -> Extraction.decompose(kind)) ~
        ("name" -> Extraction.decompose(name)) ~
        ("otherNames" -> Extraction.decompose(otherNames)) ~
        ("description" -> Extraction.decompose(description)) ~
        ("meta" -> Extraction.decompose(meta)) ~
        ("sources" -> sourcesList)
    }
  }
}
