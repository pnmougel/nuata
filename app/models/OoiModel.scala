package models

import repositories.{SourceRepository, UnitRepository}
import org.json4s.{DefaultFormats, Extraction}
import org.json4s.JsonAST.JValue
import org.json4s.JsonAST._
import play.api.libs.json.{JsObject, Json}
import org.json4s.JsonDSL._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by nico on 02/11/15.
 */
case class OoiModel(_id: Option[String],
                    _score: Option[Double],
                    name: Map[String, String],
                    otherNames: Map[String, List[String]],
                    description: Map[String, String],
                    unitIds: List[String],
                    sourceIds: List[String],
                    meta: Map[String, _])
  extends LocalizedNamedModel(_id, _score, name, otherNames, description, meta)
  with JsonSerializable {

  implicit val formats = DefaultFormats

  lazy val sources = Future.sequence(for(sourceId <- sourceIds) yield { SourceRepository.byId(sourceId) })
  val units = Future.sequence(for(unitId <- unitIds) yield { UnitRepository.byId(unitId) })

  override def getIndexQuery() = {
    defaultIndexQuery ++ Map("unitIds" -> unitIds) ++ Map("sourceIds" -> sourceIds)
  }
  override def getSearchQuery() = defaultSearchQuery
  override def getMatchQuery() = defaultMatchQuery

  def toJson : Future[JObject] = {
    for(unitsJson <- toJsonSeq(units);
        sourcesList <- toJsonSeq(sources)) yield {
        ("_id" -> _id) ~
          ("_score" -> _score) ~
          ("name" -> Extraction.decompose(name)) ~
          ("otherNames" -> Extraction.decompose(otherNames)) ~
          ("descriptions" -> Extraction.decompose(description)) ~
          ("units" -> unitsJson) ~
          ("meta" -> Extraction.decompose(meta)) ~
          ("sources" -> sourcesList)
    }
  }
}
