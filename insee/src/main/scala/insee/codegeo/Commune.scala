package insee.codegeo

import play.api.libs.json.Json

import scala.collection.mutable

/**
 * Created by nico on 10/10/15.
 */
case class Commune(name: String, chefLieuId: String, region: Region, departement: Departement, arrondissement: Option[Arrondissement], canton: Option[Canton]) {
  def asJson() = {
    var json = Json.obj("name" -> name )
    for(a <- arrondissement) {
      json ++= Json.obj("arrondissement" -> a.id)
    }
    for(a <- canton) {
      json ++= Json.obj("canton" -> a.id)
    }
    json
  }
}

object Commune {
//  val idToCommune = mutable.HashMap[String, Commune]()
  implicit val jsonFormat = Json.format[Commune]

  def load = {
    for(elems <- Utils.parseFile("comsimp2015")) {
      val rId = elems(2)
      val dId = elems(3)

      val region = Region.idToRegion(rId)
      val departement = Departement.idToDepartement(dId)
//      val id = elems(4)
      val aId = rId + dId + elems(5)
      val arrondissement = Arrondissement.idToArrondissement.get(aId)
      val cId = rId + dId + elems(6)
      val canton = Canton.idToCanton.get(cId)
      val chefLieuId = elems(1)
      val name = elems(11)
      val item = Commune(name, chefLieuId, region, departement, arrondissement, canton)
      for(c <- canton) {
        c.communes :+= item
      }
      for(c <- arrondissement) {
        c.communes :+= item
      }

//      if(idToCommune.contains(id)) {
//        println("Id commune already exists " + id)
//      }

//      idToCommune(id) = item
    }
  }

}
