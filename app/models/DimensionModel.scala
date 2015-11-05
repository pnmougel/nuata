package models

import play.api.libs.json.{JsObject, Json}
import repositories.CategoryRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


/**
 * Created by nico on 30/10/15.
 */



case class DimensionModel(
                           _id: Option[String],
                           _score: Option[Double],
                            names: Map[String, List[String]],
                           descriptions: Map[String, String],
                           categoryIds: List[String],
                           parentIds: List[String]
                           ) extends LocalizedNamedModel(_id, _score, names, descriptions) {
  val categories = Future.sequence(for(categoryId <- categoryIds) yield { CategoryRepository.byId(categoryId) })

  override def getIndexQuery() = {
    defaultIndexQuery ++ Map("categoryIds" -> categoryIds, "parentIds" -> parentIds)
  }
  override def getSearchQuery() = defaultSearchQuery
  override def getMatchQuery() = defaultMatchQuery

  def toJson : Future[JsObject] = {
    categories.flatMap( items => { Future.sequence(items.map( item => { item.toJson.map(itemJson => itemJson) })) }).map( items => {
      Json.obj(
        "names" -> names,
        "descriptions" -> descriptions,
        "categories" -> Json.toJson(items)
      )
    })
  }
}
