package queries

import models.{CategoryModel, DimensionModel}
import repositories.DimensionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
 * Created by nico on 14/10/15.
 */
case class DimensionQuery(
              id: Option[String],
              ref: String,
              names: Map[String, List[String]],
              descriptions: Map[String, List[String]],
              parents: List[String],
              categories: List[String],
              create: Option[String])
  extends SearchableItem[DimensionModel](
              "nuata" -> "dimension",
              id,
              names,
              descriptions,
              create, CreateOption.IfNameNotMatching)
  with ItemWithDependencies {

  var resolvedParents = List[DimensionQuery]()
  var resolvedCategories = List[Category]()

  lazy val categoryIds = resolveIds(resolvedCategories)
  lazy val parentIds = resolveIds(resolvedParents)

  def resolveReferences(createQuery: CreateQuery): Unit = {
    // Check if there are missing references
    for((refs, refMapping) <- List((categories, createQuery.categoryRefMapping), (parents, createQuery.dimensionRefMapping))) {
      for(ref <- refs) { hasMissingRef = hasMissingRef & refMapping.contains(ref.toLowerCase) }
    }
    resolvedCategories = for(categoryRef <- categories; category <- createQuery.categoryRefMapping.get(categoryRef.toLowerCase)) yield category
    resolvedParents = for(dimensionRef <- parents; dimension <- createQuery.dimensionRefMapping.get(dimensionRef.toLowerCase)) yield dimension
  }

  override def exactMatchQuery = Future(defaultExactMatchQuery)

  override def indexQuery = {
    for(categoryIds <- categoryIds; parentIds <- parentIds) yield {
      defaultInsertQuery ++ Map(
        "categoryIds" -> categoryIds,
        "parentIds" -> parentIds)
    }
  }
}
