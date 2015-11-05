package querybuilder

import org.json4s.JsonAST._

/**
 * Created by nico on 20/10/15.
 */
class Dimension(names: List[LocalizedString] = List(), descriptions: List[LocalizedString] = List(), val categories: List[Category] = List())
  extends JsonSerializable(names, descriptions) {

  def serialize: JValue = {
    val categoriesRefs = categories.map(category => JString(category.ref))
    JObject(JField("categories", JArray(categoriesRefs.toList)) :: baseFields)
  }
}