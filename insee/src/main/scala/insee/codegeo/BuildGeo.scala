package insee.codegeo

import play.api.libs.json.Json

import scala.collection.mutable

/**
 * Created by nico on 10/10/15.
 */
object BuildGeo {
  def main(args: Array[String]) {
    Region.load
    Departement.load
    Arrondissement.load
    Canton.load
    Commune.load

    val json = Json.toJson(Region.idToRegion.values.map(_.asJson()))
    val out = Json.prettyPrint(json)
//    println(out)

    val communes = mutable.HashSet[Commune]()
    for(region <- Region.idToRegion.values; departement <- region.departements) {
      for(arr <- departement.arrondissements) {
        for(com <- arr.communes) {
          communes.add(com)
        }
      }
      for(canton <- departement.cantons) {
        for(com <- canton.communes) {
          communes.add(com)
        }
      }
    }
    println(communes.size)
  }
}
