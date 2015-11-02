package models

import com.sksamuel.elastic4s.ElasticDsl._
import models.CreateOption._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
 * Created by nico on 14/10/15.
 */
case class Dimension(
              id: Option[String],
              ref: String,
              names: Map[String, List[String]],
              descriptions: Map[String, List[String]],
              parents: List[String],
              categories: List[String],
              create: Option[String])
  extends SearchableItem(
              "nuata" -> "dimension",
              id,
              names,
              descriptions,
              create, CreateOption.IfNameNotMatching) {

  var allParents = List[Dimension]()
  var allCategories = List[Category]()

  def buildRefMapping(createQuery: CreateQuery): Unit = {
    createQuery.dimensionRefMapping(ref.toLowerCase) = this
  }

  def resolveDependencies(createQuery: CreateQuery): Unit = {
    allCategories = for(categoryRef <- categories; category <- createQuery.categoryRefMapping.get(categoryRef.toLowerCase)) yield category
    allParents = for(dimensionRef <- parents; dimension <- createQuery.dimensionRefMapping.get(dimensionRef.toLowerCase)) yield dimension
  }

  override def getMatchQuery = {
    Future(defaultMatchQueries)
  }

  override def getTermQuery = {
    Future(defaultTermQueries)
  }

  override def insertQuery = {
    getSearchIds(allCategories).map { catIds =>
      val ids = for(idOpt <- catIds; id <- idOpt) yield { id }
      val newQuery = defaultInsertQuery.clone()
      newQuery("categoryIds") = ids
      newQuery
    }
  }
}
