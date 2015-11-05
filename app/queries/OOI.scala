package queries

import models.{OoiModel, CategoryModel}
import repositories.OoiRepository._
import repositories.{OoiRepository, CategoryRepository}

import scala.collection.immutable.HashMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by nico on 14/10/15.
 */
case class OOI(
              id: Option[String],
              ref:  String,
              names: Map[String, List[String]],
              descriptions: Map[String, List[String]],
              units: List[String],
              create: Option[String])
  extends SearchableItem[OoiModel](
              "nuata" -> "ooi",
              id,
              names,
              descriptions,
              create, CreateOption.IfNameNotMatching)
  with ItemWithDependencies {

  var resolvedUnits = List[FactUnit]()

  lazy val unitIds = resolveIds(resolvedUnits)

  def resolveReferences(createQuery: CreateQuery): Unit = {
    for(ref <- units) { hasMissingRef = hasMissingRef & createQuery.factUnitRefMapping.contains(ref.toLowerCase) }
    resolvedUnits = for(ref <- units; item <- createQuery.factUnitRefMapping.get(ref.toLowerCase)) yield item
  }
//
//  def resolve: Future[Option[OoiModel]] = {
//    OoiRepository.filteredQuery(exactMatchQuery).map( items => {
//      if(items.length == 1) Some(items(0)) else None
//    })
//  }

  override def exactMatchQuery = Future(defaultExactMatchQuery)

  override def indexQuery = {
    for(unitIds <- unitIds) yield {
      defaultInsertQuery ++ Map("unitIds" -> unitIds)
    }
  }

  /*
  def resolveDependencies(createQuery: CreateQuery): Unit = {
    allFactUnits = for(unitRef <- units; factUnit <- createQuery.factUnitRefMapping.get(unitRef.toLowerCase)) yield factUnit
//    allFactUnits = for(factUnit <- allFactUnits) yield {
//      if(factUnit.ref.isDefined && createQuery.factUnitRefMapping.contains(factUnit.ref.get)) {
//        createQuery.factUnitRefMapping(factUnit.ref.get)
//      } else {
//        createQuery.units = factUnit :: createQuery.units
//        factUnit
//      }
//    }
  }
  */

  /*
  override def insertQuery = {
    getSearchIds(resolvedUnits).map { unitIds =>
      val ids = for(idOpt <- unitIds; id <- idOpt) yield { id }
      val newQuery = defaultInsertQuery.clone()
      newQuery("unitIds") = ids
      newQuery
    }
  }
  */
}
