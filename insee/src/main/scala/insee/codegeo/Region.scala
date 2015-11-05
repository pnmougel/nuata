package insee.codegeo

import play.api.libs.json.Json

import scala.collection.mutable

/**
 * Created by nico on 10/10/15.
 */
case class Region(name: String, id: String, chefLieuId: String) {
  var departements = Vector[Departement]()
  def asJson() = {
    Json.obj(
      "name" -> name,
      "id" -> id,
      "departements" -> departements.map(_.asJson)
      )
  }
}

object Region {
  val idToRegion = mutable.HashMap[String, Region]()
  implicit val jsonFormat = Json.format[Region]

  def load = {
    for(elems <- Utils.parseFile("reg2015")) {
      val id = elems(0)
      val chefLieuId = elems(1)
      val name = elems(4)
      idToRegion(id) = Region(name, id, chefLieuId)
    }
  }
}
