package querybuilder.serializers

import org.json4s.CustomSerializer
import org.json4s.JsonAST.JValue
import querybuilder.JsonSerializable

/**
 * Created by nico on 19/11/15.
 */
abstract class ItemSerializable extends CustomSerializer[JsonSerializable](format => (
{ case _ => null },
{ case serializable: JsonSerializable =>
serializable.serialize
})) {

  /**
   * Id of the item
   */
  var _id: Option[String] = None

  def serialize: JValue
}
