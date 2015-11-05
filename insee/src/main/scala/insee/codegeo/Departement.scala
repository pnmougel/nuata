package insee.codegeo

import play.api.libs.json.Json

import scala.collection.mutable

/**
 * Created by nico on 10/10/15.
 */
case class Departement(id: String, name: String, idChefLieu: String, region: Region) {
  var arrondissements = Vector[Arrondissement]()
  var cantons = Vector[Canton]()
  def asJson() = {
    Json.obj(
      "name" -> name,
      "id" -> id,
      "arrondissements" -> arrondissements.map(_.asJson),
      "cantons" -> cantons.map(_.asJson)
    )
  }
}

object Departement {
  val idToDepartement = mutable.HashMap[String, Departement]()
  implicit val jsonFormat = Json.format[Departement]

  def load = {
    for(elems <- Utils.parseFile("depts2015")) {
      val region = Region.idToRegion(elems(0))
      val id = elems(1)
      val chefLieuId = elems(2)
      val name = elems(5)
      val item = Departement(id, name, chefLieuId, region)
      region.departements :+= item
      if(idToDepartement.contains(id)) {
        println("Id departement already exists " + id)
      }

      idToDepartement(id) = item
    }
  }
}
