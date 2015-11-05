package querybuilder

import org.json4s.JsonAST._

import scala.util.Random

/**
 * Created by nico on 20/10/15.
 */
class OOI(names: List[LocalizedString] = List(), descriptions: List[LocalizedString] = List(), units: List[FactUnit] = List())
  extends JsonSerializable(names, descriptions) {

  def serialize: JValue = {
    val unitsRefs = units.map(unit => JString(unit.ref))
    JObject(JField("units", JArray(unitsRefs.toList)) :: baseFields)

//    val namesJson = localizedStringToJson(names, "name")
//    val descJson = localizedStringToJson(descriptions, "description")
//    val unitsRefs = units.map(unit => JString(unit.ref))
//    JObject(
//      JField("ref", JString(ref))
//        :: JField("names", JArray(namesJson))
//        :: JField("descriptions", JArray(descJson))
//        :: JField("units", JArray(unitsRefs.toList))
//        :: Nil)
  }
}