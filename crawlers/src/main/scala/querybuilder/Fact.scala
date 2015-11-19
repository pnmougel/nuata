package querybuilder

import java.text.SimpleDateFormat
import java.util.{TimeZone, Date}

import org.json4s.JsonAST._
import querybuilder.serializers.ItemSerializable

/**
 * Created by nico on 20/10/15.
 */
class Fact(val value: Option[Double], val valueLong: Option[Long], val dimensions: List[Dimension], val ooi: OOI, val at: Option[Date] = None)
  extends ItemSerializable
  with ItemWithDependencies {

  def serialize = {
    val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    df.setTimeZone(TimeZone.getTimeZone("UTC"))

    val dimensionIds = getIds(dimensions)
    val ooiIds = getIds(List(ooi))
    var fields = JField("dimensionIds", dimensionIds) :: JField("ooiIds", ooiIds) :: Nil
    for(date <- at) { fields = JField("at", JString(df.format(date))) :: fields }
    for(v <- value) { fields = JField("value", JDouble(v)) :: fields }
    for(v <- valueLong) { fields = JField("valueInt", JLong(v)) :: fields }
    JObject(fields)
  }
}
