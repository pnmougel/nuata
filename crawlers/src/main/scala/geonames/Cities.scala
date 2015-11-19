package geonames

import scala.io.Source

/**
 * Created by nico on 18/11/15.
 */
object Cities {
  def read: Unit = {

    for ((line, i) <- Source.fromFile("/home/nico/data/geonames/cities15000.txt").getLines().zipWithIndex) {
      if (i % 100000 == 0) {
        println(i)
      }
      val entry = GeoNames.readGeonameEntry(line)

    }
  }

//  def readCountries() = {
//    for ((line, i) <- Source.fromFile("/home/nico/data/geonames/allCountries.txt").getLines().zipWithIndex) {
//      if (i % 100000 == 0) {
//        println(i)
//      }
//      val entry = GeoNames.readGeonameEntry(line)
//
//    }
//  }
}
