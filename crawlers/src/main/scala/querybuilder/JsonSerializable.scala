package querybuilder

import org.json4s.CustomSerializer
import org.json4s.JsonAST._

import scala.collection.mutable
import scala.util.Random

/**
 * Created by nico on 20/10/15.
 */
abstract class JsonSerializable(names: List[LocalizedString], descriptions: List[LocalizedString])
  extends CustomSerializer[JsonSerializable](format => (
  { case _ => null },
  { case serializable: JsonSerializable =>
    serializable.serialize
  })) {

  lazy val ref = {
    val alphaNum = ('0' to '9') ++ ('a' to 'z') ++ ('A' to 'Z')
    val code = (for(i <- 0 until 8) yield { alphaNum(Random.nextInt(alphaNum.length)) }).mkString("")
    if(names.headOption.isEmpty) {
      code
    } else {
      names.head.str.replaceAll(" ", "_").toLowerCase() + "_" + code
    }
  }

  def localizedStringToJson(localizedStrings : List[LocalizedString], fieldName: String) = {
    val langToNames = mutable.HashMap[String, List[JValue]]()
    for(localizedString <- localizedStrings) {
      langToNames(localizedString.lang) = JString(localizedString.str) :: langToNames.getOrElse(localizedString.lang, List[JValue]())
    }
    JObject(for((k, v) <- langToNames.toList) yield {
      JField(k, JArray(v))
    })
  }

  val baseFields : List[JField] = {
    JField("names", localizedStringToJson(names, "name")) ::
      JField("descriptions", localizedStringToJson(descriptions, "description")) ::
      JField("ref", JString(ref)) :: Nil
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