package querybuilder.serializers

import java.text.SimpleDateFormat
import java.util.TimeZone

import org.json4s.CustomSerializer
import org.json4s.JsonAST._
import querybuilder.Fact

/**
 * Created by nico on 20/10/15.
 */

class FactSerializer extends CustomSerializer[Fact](format => (
  { case _ => null },
  { case fact: Fact =>
    val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    df.setTimeZone(TimeZone.getTimeZone("UTC"))

    val dimensionIds = fact.getIds(fact.dimensions)
    val ooiIds = fact.getIds(List(fact.ooi))
    var fields = JField("dimensionIds", dimensionIds) :: JField("ooiIds", ooiIds) :: Nil
//    val dimensionsRefs = fact.dimensions.map(dimension => JString(dimension.ref))
//    val ooiRef = JString(fact.ooi.ref)
//    var fields = JField("dimensions", JArray(dimensionsRefs)) :: JField("ooi", ooiRef) :: Nil
    for(date <- fact.at) { fields = JField("at", JString(df.format(date))) :: fields }
    for(v <- fact.value) { fields = JField("value", JDouble(v)) :: fields }
    for(v <- fact.valueLong) { fields = JField("valueInt", JLong(v)) :: fields }
    JObject(fields)
  }))