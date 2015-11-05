package queries

import models.{UnitModel, FactModel, CategoryModel}
import repositories.{UnitRepository, FactRepository, CategoryRepository}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
/**
 * Created by nico on 14/10/15.
 */
case class FactUnit(
    id: Option[String],
    ref:  String,
    names: Map[String, List[String]],
    descriptions: Map[String, List[String]],
    create: Option[String])
  extends SearchableItem[UnitModel](
    "nuata" -> "unit",
    id,
    names,
    descriptions,
    create, CreateOption.IfNameNotMatching) {

  def indexQuery() = Future(defaultInsertQuery)

  def exactMatchQuery = Future(defaultExactMatchQuery)
}
