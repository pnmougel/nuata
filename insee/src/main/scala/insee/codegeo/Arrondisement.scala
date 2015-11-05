package insee.codegeo

import play.api.libs.json.Json

import scala.collection.mutable

/**
 * Created by nico on 10/10/15.
 */
case class Arrondissement(name: String, id: String, chefLieuId: String, region: Region, departement: Departement) {
  var communes = Vector[Commune]()
  def asJson() = {
    Json.obj(
      "name" -> name,
      "id" -> id,
      "communes" -> communes.map(_.asJson)
    )
  }
}

object Arrondissement {
  implicit val jsonFormat = Json.format[Arrondissement]

  val idToArrondissement = mutable.HashMap[String, Arrondissement]()

  def load = {
    for(elems <- Utils.parseFile("arrond2015")) {
      val rId = elems(0)
      val dId = elems(1)

      val region = Region.idToRegion(rId)
      val departement = Departement.idToDepartement(dId)
      val id = rId + dId + elems(2)
      val chefLieuId = elems(3)
      val name = elems(8)
      val item = Arrondissement(name, elems(2), chefLieuId, region, departement)
      departement.arrondissements :+= item
      if(idToArrondissement.contains(id)) {
        println("Id arrondissement already exists " + id)
      }

      idToArrondissement(id) = item
    }
  }


}
