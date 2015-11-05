package insee.codegeo

import play.api.libs.json.Json

import scala.collection.mutable

/**
 * Created by nico on 10/10/15.
 */
case class Canton(name: String, id: String, region: Region, departement: Departement) {
  var communes = Vector[Commune]()
  def asJson() = {
    Json.obj(
      "name" -> name,
      "id" -> id,
      "communes" -> communes.map(_.asJson)
    )
  }
}

object Canton {
  val idToCanton = mutable.HashMap[String, Canton]()
  implicit val jsonFormat = Json.format[Canton]

  def load = {
    for(elems <- Utils.parseFile("canton2015")) {
      val rId = elems(0)
      val dId = elems(1)
      val region = Region.idToRegion(rId)
      val departement = Departement.idToDepartement(dId)
      val id = rId + dId + elems(2)
      val name = elems(9)
      val item = Canton(name, elems(2), region, departement)
      departement.cantons :+= item


      if(idToCanton.contains(id)) {
        println("Id canton already exists " + id)
      }
      idToCanton(id) = item
    }
  }

}
