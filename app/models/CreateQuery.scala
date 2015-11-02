package models


import scala.collection.mutable

/**
 * Created by nico on 13/10/15.
 */

case class CreateQuery(
            categories: List[Category],
            dimensions: List[Dimension],
            units: List[FactUnit],
            oois: List[OOI],
            facts: List[Fact],
            create: Option[String]) {

  val createOption = CreateOption.fromString(create)

  // Mappings
  val categoryRefMapping = mutable.HashMap[String, Category]()
  val dimensionRefMapping = mutable.HashMap[String, Dimension]()
  val factUnitRefMapping = mutable.HashMap[String, FactUnit]()
  val ooiRefMapping = mutable.HashMap[String, OOI]()

  val allItems = categories ::: units ::: dimensions ::: oois
  val itemsWithDependencies = allItems ::: facts


  // Build the mappings between a ref and an item
  for(item <- allItems) { item.buildRefMapping(this) }

  // Resolve the dependencies and updates the set of items
  for(item <- itemsWithDependencies) { item.resolveDependencies(this) }

  // Init the create options
  for(item <- allItems) {
    item.initCreateOption(this)
  }
}
