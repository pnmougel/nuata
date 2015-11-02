package models

import scala.concurrent.ExecutionContext.Implicits.global

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
  extends SearchableItem(
              "nuata" -> "ooi",
              id,
              names,
              descriptions,
              create, CreateOption.IfNameNotMatching) {

//  var allFactUnits = if(unit.isDefined) unit.get :: units else units
  var allFactUnits = List[FactUnit]()

  def buildRefMapping(createQuery: CreateQuery): Unit = {
    createQuery.ooiRefMapping(ref.toLowerCase) = this
  }

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

  override def insertQuery = {
    getSearchIds(allFactUnits).map { unitIds =>
      val ids = for(idOpt <- unitIds; id <- idOpt) yield { id }
      val newQuery = defaultInsertQuery.clone()
      newQuery("unitIds") = ids
      newQuery
    }
  }
}
