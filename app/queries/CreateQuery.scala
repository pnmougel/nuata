package queries

import scala.collection.mutable
import scala.concurrent.Future

/**
 * Created by nico on 13/10/15.
 */

case class CreateQuery(
            categories: List[Category],
            dimensions: List[DimensionQuery],
            units: List[FactUnit],
            oois: List[OOI],
            facts: List[Fact],
            create: Option[String]) {

  val createOption = CreateOption.fromString(create)

  // Mappings
  val categoryRefMapping = Map((for(item <- categories) yield (item.ref.toLowerCase, item)): _*)
  val dimensionRefMapping = Map((for(item <- dimensions) yield (item.ref.toLowerCase, item)): _*)
  val factUnitRefMapping = Map((for(item <- units) yield (item.ref.toLowerCase, item)): _*)
  val ooiRefMapping = Map((for(item <- oois) yield (item.ref.toLowerCase, item)): _*)

  val categoryRefResults = mutable.HashMap[String, Future[SearchResult]]()

  val allItems = categories ::: units ::: dimensions ::: oois
  val itemsWithDependencies = dimensions ::: oois ::: facts

  // Resolve the dependencies and updates the set of items
  for(item <- itemsWithDependencies) { item.resolveReferences(this) }

  // Init the create options
  for(item <- allItems) {
    item.initCreateOption(this)
  }
}
