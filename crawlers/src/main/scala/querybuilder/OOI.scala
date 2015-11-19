package querybuilder

import org.json4s.JsonAST._

import scala.util.Random

/**
 * Created by nico on 20/10/15.
 */
class OOI(names: List[LocalizedString] = List(), descriptions: List[LocalizedString] = List(), units: List[FactUnit] = List())
  extends JsonSerializable(names, descriptions, "ooi")
  with ItemWithDependencies
{
  def serialize: JValue = {
    JObject(JField("unitIds", getIds(units)) :: baseFields)
  }
}