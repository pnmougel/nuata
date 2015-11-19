package geonames

/**
 * Created by nico on 18/11/15.
 */
case class GeonameEntry(id: Int, name: String, asciiName: String, alternateNames: Array[String],
                        latitude: Double, longitude: Double,
                        featureClass: String, featureCode: String, countryCode: String, cc2: String,
                        adminCode1: String, adminCode2: String, adminCode3: String, adminCode4: String,
                        population: Option[Long], elevation: Option[Int], dem: String, timezone: String, modificationDate: String) {
  override def toString(): String = {
    List("------------",
      s"id: ${id}",
      s"name: ${name}",
      s"asciiName: ${asciiName}",
      s"countryCode: ${countryCode}",
      s"cc2: ${cc2}",
      s"adminCodes: ${adminCode1} - ${adminCode2} - ${adminCode3} - ${adminCode4}",
      s"population: ${population}",
      s"elevation: ${elevation}",
      s"dem: ${dem}",
      s"timezone: ${timezone}",
      s"alternateNames: ${alternateNames.mkString(",")}",
      s"latitude: ${latitude}",
      s"longitude: ${longitude}",
      s"feature: ${featureClass} ${featureCode}"
    ).mkString("\n")
  }
}
