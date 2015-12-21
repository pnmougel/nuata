package models

import org.json4s.Extraction
import repositories.{SourceRepository, OoiRepository, DimensionRepository}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import org.json4s._
import org.json4s.JsonAST._
import org.json4s.JsonDSL._

/**
 * Created by nico on 02/11/15.
 */
case class FactModel(_id: Option[String], _score: Option[Double],
                     ooiIds: List[String], dimensionIds: List[String], sourceIds: List[String],
                     at: Option[java.util.Date],
                     value: Option[Double], valueInt: Option[Long],
                     meta: Map[String, _])
  extends BaseModel(_id, _score)
  with JsonSerializable {

  implicit val formats = DefaultFormats

  val oois = Future.sequence(for(ooiId <- ooiIds) yield { OoiRepository.byId(ooiId) })
  val dimensions = Future.sequence(for(dimensionId <- dimensionIds) yield { DimensionRepository.byId(dimensionId) })
  val sources = Future.sequence(for(sourceId <- sourceIds) yield { SourceRepository.byId(sourceId) })

  def toJson: Future[JObject] = {
    for(ooisJson <- toJsonSeq(oois); dimensionsJson <- toJsonSeq(dimensions); sourceJson <- toJsonSeq(sources)) yield {
      ("value" -> value) ~
        ("valueInt" -> valueInt) ~
        ("oois" -> ooisJson) ~
        ("dimensions" -> dimensionsJson) ~
        ("meta" -> Extraction.decompose(meta)) ~
        ("sources" -> sourceJson)
    }
  }
}
