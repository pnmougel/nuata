package models

import repositories.SourceRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import org.json4s._
import org.json4s.JsonAST._
import org.json4s.JsonDSL._

/**
 * Created by nico on 02/11/15.
 */
case class CategoryModel(_id: Option[String],
                         _score: Option[Double],
                         name: Map[String, String],
                         otherNames: Map[String, List[String]],
                         description: Map[String, String],
                         sourceIds: List[String],
                         meta: Map[String, _])
  extends LocalizedNamedModel(_id, _score, name, otherNames, description, meta)
  with JsonSerializable {

  lazy val sources = Future.sequence(for(sourceId <- sourceIds) yield { SourceRepository.byId(sourceId) })

  implicit val formats = DefaultFormats

//  override def getIndexQuery() = defaultIndexQuery
  override def getSearchQuery() = defaultSearchQuery
  override def getMatchQuery() = defaultMatchQuery

  override def getIndexQuery() = {
    defaultIndexQuery ++ Map("sourceIds" -> sourceIds)
  }

  def toJson : Future[JObject] = {
    for(sourcesList <- toJsonSeq(sources)) yield {
      ("_id" -> _id) ~ ("_score" -> _score) ~
        ("name" -> Extraction.decompose(name)) ~
        ("otherNames" -> Extraction.decompose(otherNames)) ~
        ("descriptions" -> Extraction.decompose(description)) ~
        ("meta" -> Extraction.decompose(meta)) ~
        ("sources" -> sourcesList)
    }
  }
}
