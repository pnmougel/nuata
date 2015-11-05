package querybuilder

import java.util.Date

import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._
import querybuilder.serializers.{QuerySerializer, FactSerializer}

/**
 * Created by nico on 20/10/15.
 */
class Query() {
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

  def addDimension(names: List[LocalizedString], descriptions: List[LocalizedString] = List(), categories: List[Category]): Dimension = {
    val newDimension = new Dimension(names, descriptions, categories)
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

  implicit val formats = Serialization.formats(NoTypeHints) + new QuerySerializer()
  def toJson: String = write(this)

  def toPrettyJson: String = writePretty(this)
}
