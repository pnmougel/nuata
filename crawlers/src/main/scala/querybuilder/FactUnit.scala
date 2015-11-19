package querybuilder

import org.json4s.JsonAST._

/**
 * Created by nico on 20/10/15.
 */
class FactUnit(names: List[LocalizedString] = List(), descriptions: List[LocalizedString] = List())
  extends JsonSerializable(names, descriptions, "unit")
{
  def serialize: JValue = JObject(baseFields)
}
