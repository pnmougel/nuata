package querybuilder

import org.json4s.CustomSerializer
import org.json4s.JsonAST._
import querybuilder.serializers.ItemSerializable

import scala.collection.mutable
import scala.util.Random

/**
 * Created by nico on 20/10/15.
 */
abstract class JsonSerializable(names: List[LocalizedString], descriptions: List[LocalizedString], kind: String)
  extends ItemSerializable
{

  def localizedStringToJson(localizedStrings : List[LocalizedString], fieldName: String) = {
    val langToNames = mutable.HashMap[String, List[JValue]]()
    for(localizedString <- localizedStrings) {
      langToNames(localizedString.lang) = JString(localizedString.str) :: langToNames.getOrElse(localizedString.lang, List[JValue]())
    }
    JObject(for((k, v) <- langToNames.toList) yield {
      JField(k, JArray(v))
    })
  }


  def localizedDescriptionToJson(localizedStrings : List[LocalizedString], fieldName: String) = {
    JObject(for(localizedString <- localizedStrings) yield {
      JField(localizedString.lang, JString(localizedString.str))
    })
  }


  def nameToString = {
    names.map(x => s"${x.str}/${x.lang}").mkString(" - ")
  }

  val baseFields : List[JField] = {
    JField("names", localizedStringToJson(names, "name")) ::
      JField("descriptions", localizedDescriptionToJson(descriptions, "description")) ::
//      JField("ref", JString(ref)) ::
      Nil
  }
  def serialize: JValue
}

/*
abstract class JsonSerializableNames(val names: List[LocalizedString] = List(), val descriptions: List[LocalizedString] = List()) extends JsonSerializable {
  def ref: String = {
    names(0).str
  }

  def serialize: JValue = {
    val namesJson = localizedStringToJson(names, "name")
    val descJson = localizedStringToJson(descriptions, "description")
    JObject(
      JField("names", JArray(namesJson)) ::
        JField("descriptions", JArray(descJson)) ::
        JField("ref", JString(ref)) :: Nil)
  }
}
*/