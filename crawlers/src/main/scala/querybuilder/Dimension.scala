package querybuilder

import org.json4s.JsonAST._

/**
 * Created by nico on 20/10/15.
 */
class Dimension(names: List[LocalizedString] = List(), descriptions: List[LocalizedString] = List(), val categories: List[Category] = List(), val parents: List[Dimension] = List())
  extends JsonSerializable(names, descriptions, "dimension")
  with ItemWithDependencies
{

  override def serialize: JValue = {
    JObject(JField("categoryIds", getIds(categories)) :: JField("parentIds", getIds(parents)) :: baseFields)
  }
}