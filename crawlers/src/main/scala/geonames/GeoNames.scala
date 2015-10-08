package geonames

import services.Categories

import scala.collection.mutable
import scala.io.Source

object GeoNames {
  val featureClassWithMissingFeature = mutable.HashMap[String, Int]()

  def readCountries() = {
    for((line, i) <- Source.fromFile("/home/nico/data/geonames/allCountries.txt").getLines().zipWithIndex) {
      if(i % 100000 == 0) {
//        println(i)
      }
      val elems = line.split("\t")
      val nbElems = elems.length
      if(nbElems != 19) {
        println("Invalid line")
        println(line)
      }
      val id = elems(0).toInt
      val name = elems(1)
      val asciiName = elems(2)
      val alternateNames = elems(3).split(",")
      val latitude = elems(4).toDouble
      val longitude = elems(5).toDouble
      val featureClass = elems(6)
      val featureCode = elems(7)
      val countryCode = elems(8)
      val cc2 = elems(9)
      val adminCode1 = elems(10)
      val adminCode2 = elems(11)
      val adminCode3 = elems(12)
      val adminCode4 = elems(13)
      val population = if(elems(14).isEmpty) None else Some(elems(14).toLong)
      val elevation = if(elems(15).isEmpty) None else Some(elems(15).toInt)
      val dem = elems(16)
      val timezone = elems(17)
      val modificationDate = elems(18)

      val entry = GeonameEntry(id, name, asciiName, alternateNames, latitude, longitude, featureClass, featureCode, countryCode, cc2,
        adminCode1, adminCode2, adminCode3, adminCode4, population, elevation, dem, timezone, modificationDate)

      if(name.toLowerCase() == "france") {
        println(entry)
      }

//      features((featureClass, featureCode))
    }
    println(featureClassWithMissingFeature)
  }



  def main(args: Array[String]) : Unit = {
//    readFeatures()
    readCountries()
  }
}

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