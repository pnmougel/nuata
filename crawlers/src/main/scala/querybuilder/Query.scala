package querybuilder

import java.util.Date

import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._
import querybuilder.serializers.{ItemSerializable, QuerySerializer, FactSerializer}

import scala.collection.mutable
import scalaj.http.Http

/**
 * Created by nico on 20/10/15.
 */
class Query(server: String = "http://localhost:9000") {
  var categories = Vector[Category]()
  var dimensions = Vector[Dimension]()
  var oois = Vector[OOI]()
  var units = Vector[FactUnit]()
  var facts = Vector[Fact]()

  def addCategory(names: List[LocalizedString], descriptions: List[LocalizedString] = List()): Category = {
    if(names.isEmpty) { println("Empty category names") }
    val newCategory = new Category(names, descriptions)
    categories :+= newCategory
    newCategory
  }

  def addDimension(names: List[LocalizedString], descriptions: List[LocalizedString] = List(), categories: List[Category], parents: List[Dimension] = List()): Dimension = {
    val newDimension = new Dimension(names, descriptions, categories, parents)
    dimensions :+= newDimension
    newDimension
  }

  def addOOI(names: List[LocalizedString], descriptions: List[LocalizedString] = List(), units: List[FactUnit]): OOI = {
    val newOOI = new OOI(names, descriptions, units)
    oois :+= newOOI
    newOOI
  }

  def addUnit(names: List[LocalizedString], descriptions: List[LocalizedString] = List()): FactUnit = {
    val newUnit = new FactUnit(names, descriptions)
    units :+= newUnit
    newUnit
  }

  def addFact(value: Double, dimensions: List[Dimension], ooi: OOI, at: Option[Date]): Fact = {
    val newFact = new Fact(Some(value), None, dimensions, ooi, at)
    facts :+= newFact
    newFact
  }
  def addFact(value: Long, dimensions: List[Dimension], ooi: OOI, at: Option[Date]): Fact = {
    val newFact = new Fact(None, Some(value), dimensions, ooi, at)
    facts :+= newFact
    newFact
  }
  case class MappedIds(_id: List[String])

  implicit val formats = Serialization.formats(NoTypeHints) // + new QuerySerializer()


  var dimensionsDep = Vector[Vector[ItemSerializable]]()
  def buildDependencies = {
    var dimensionsToPrune = List(dimensions :_*)
    val addedItems = mutable.HashSet[ItemSerializable]()
    while(!dimensionsToPrune.isEmpty) {
      var resolvedDimensions = Vector[ItemSerializable]()
      dimensionsToPrune = dimensionsToPrune.filter(dimension => {
        val allDepResolved = !dimension.parents.exists( p => !addedItems.contains(p))
        if(allDepResolved) {
          resolvedDimensions :+= dimension
          addedItems.add(dimension)
        }
        !allDepResolved
      })

      // Send several queries when the number of items is large
      for(splitItems <- splitItemsAt(resolvedDimensions, 10000)) {
        dimensionsDep :+= splitItems
      }
//      var curItemsToAdd = Vector[Dimension]()
//      for((d, i) <- resolvedDimensions.zipWithIndex) {
//        if(i % 10000 == 0 && i != 0) {
//          dimensionsDep :+= curItemsToAdd
//          curItemsToAdd = Vector[Dimension]()
//        }
//        addedItems.add(d)
//        curItemsToAdd :+= d
//      }
//      dimensionsDep :+= curItemsToAdd
    }
  }

  def sendItems(items: Seq[ItemSerializable], kind: String) = {
    val data = write(items.map(item => item.serialize))
    val res = Http(s"${server}/${kind}/index").postData(data).header("content-type", "application/json").asString
    val ids = read[MappedIds](res.body)
    for((item, id) <- items.zip(ids._id)) {
      item._id = Some(id)
    }
  }

  def send() = {
    buildDependencies
    sendItems(categories, "category")
    sendItems(units, "unit")
    for(dimensions <- dimensionsDep) {
      sendItems(dimensions, "dimension")
    }
    sendItems(oois, "ooi")

    println("Sending facts " + facts.size)
    for(factsSplit <- splitItemsAt(facts, 10000)) {
      println("Sending")
      sendItems(factsSplit, "fact")
    }
  }

  def splitItemsAt(items: Vector[ItemSerializable], at: Int) : Vector[Vector[ItemSerializable]] = {
    var curItemsToAdd = Vector[ItemSerializable]()
    var splittedItems = Vector[Vector[ItemSerializable]]()
    for((item, idx) <- items.zipWithIndex) {
      if(idx % at == 0 && idx != 0) {
        splittedItems :+= curItemsToAdd
        curItemsToAdd = Vector[ItemSerializable]()
      }
      curItemsToAdd :+= item
    }
    splittedItems
  }

//  def toJson: String = write(this)

//  def toPrettyJson: String = writePretty(this)
}
