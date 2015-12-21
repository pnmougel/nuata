package models

import com.sksamuel.elastic4s.ElasticDsl._
import repositories.{SourceRepository, DimensionRepository, CategoryRepository}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import org.json4s._
import org.json4s.JsonAST._
import org.json4s.JsonDSL._


/**
 * Created by nico on 30/10/15.
 */
case class DimensionModel(_id: Option[String],
                          _score: Option[Double],
                          name: Map[String, String],
                          otherNames: Map[String, List[String]],
                          description: Map[String, String],
                          categoryIds: List[String],
                          parentIds: List[String],
                          sourceIds: List[String],
                          meta: Map[String, _])
  extends LocalizedNamedModel(_id, _score, name, otherNames, description, meta)
  with JsonSerializable {

  implicit val formats = DefaultFormats

  lazy val categories = Future.sequence(for(categoryId <- categoryIds) yield { CategoryRepository.byId(categoryId) })
  lazy val parents = Future.sequence(for(parentId <- parentIds) yield { DimensionRepository.byId(parentId) })
  lazy val sources = Future.sequence(for(sourceId <- sourceIds) yield { SourceRepository.byId(sourceId) })

  override def getIndexQuery() = {
    defaultIndexQuery ++ Map("categoryIds" -> categoryIds, "parentIds" -> parentIds, "sourceIds" -> sourceIds)
  }
  override def getSearchQuery = defaultSearchQuery // must { termQuery("categoryIds", "AVEAaeytsFQ7gjHpyznm") }
  override def getMatchQuery() = defaultMatchQuery

  def toJson : Future[JObject] = {
    for(categoriesList <- toJsonSeq(categories);
        parentsList <- toJsonSeq(parents);
        sourcesList <- toJsonSeq(sources)) yield {
      ("_id" -> _id) ~
        ("_score" -> _score) ~
        ("name" -> Extraction.decompose(name)) ~
        ("otherNames" -> Extraction.decompose(otherNames)) ~
        ("description" -> Extraction.decompose(description)) ~
        ("categories" -> categoriesList) ~
        ("parents" -> parentsList) ~
        ("meta" -> Extraction.decompose(meta)) ~
        ("sources" -> sourcesList)
    }
  }
}
