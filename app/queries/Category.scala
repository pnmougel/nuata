package queries

import models.CategoryModel
import repositories.CategoryRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by nico on 13/10/15.
 */
case class Category(
              id: Option[String],
              ref: String,
              names: Map[String, List[String]],
              descriptions: Map[String, List[String]],
              create: Option[String])
  extends SearchableItem[CategoryModel](
              "nuata" -> "category",
              id,
              names,
              descriptions,
              create, CreateOption.IfNameNotMatching) {

  def exactMatchQuery = Future(defaultExactMatchQuery)

  def indexQuery() = Future(defaultInsertQuery)
}
