package geonames

import services._

import scala.collection.mutable
import scala.io.Source

/**
 * Created by nico on 02/10/15.
 */
case class Country(name: String, iso: String, iso3: String, isoNumeric: String, fips: String,
                    capital: String, area: Double, population: Int, continent: String, tld: String,
                    currencyCode: String, currencyName: String, phone: String, postalCodeFormat: String,
                    languages: Array[String], geonameId: Option[Int], neighbours: Array[String],
                    equivalentFipsCode: Option[String])

object Country {
  val continentCodeToName = Map(
    "AF" -> List(Lang.en("Africa"), Lang.fr("Afrique")),
    "OC" -> List(Lang.en("Australia"), Lang.fr("Océanie")),
    "EU" -> List(Lang.en("Europe"), Lang.fr("Europe")),
    "AN" -> List(Lang.en("Antartica"), Lang.fr("Antartique")),
    "SA" -> List(Lang.en("South America"), Lang.fr("Amérique du sud")),
    "NA" -> List(Lang.en("North America"), Lang.fr("Amérique du nord")),
    "AS" -> List(Lang.en("Asia"), Lang.fr("Asie")))

  def main(args: Array[String]) = {
    read()
    insert()
  }

  val continents = mutable.HashSet[String]()
  val currencies = mutable.HashSet[String]()
  var countries = List[Country]()

  def insert() = {

    // Create objects of interests
    var resOOI = OOIs.create("Area", "square meter", Some("Surface of an area expressed in square meters"))
    val areaOOIId = resOOI.id.get
    resOOI = OOIs.create("Population", "person", Some("A number of person"))
    val populationOOIId = resOOI.id.get

    // Create continents
    for(continent <- continents) {
      Dimensions.registerDimension(continentCodeToName(continent),
        List(GNCat.areaInMapCategory, GNCat.continentCategory))
    }

    // Create currencies
    for(currency <- currencies) {
      Dimensions.registerDimension(List(Lang.en(currency)), List(GNCat.currencyCategory))
    }
    Dimensions.update()

    // Create countries and their capitals
    for(country <- countries) {
      val resCountry = Dimensions.registerDimension(List(Lang.en(country.name)),
        List(GNCat.areaInMapCategory, GNCat.countryCategory))
      Dimensions.registerDimension(List(Lang.en(country.capital)),
        List(GNCat.areaInMapCategory, GNCat.cityCategory, GNCat.capitalCategory))
      Dimensions.update()

      Facts.create(country.area, List(resCountry.id.get), areaOOIId)
      Facts.create(country.population, List(resCountry.id.get), populationOOIId)
    }
  }

  def read() = {
    for(line <- Source.fromFile("/home/nico/data/geonames/countryInfo.txt").getLines(); if !line.startsWith("#")) {
      val elems = line.split("\t")
      val continent = elems(8)
      val currencyName = elems(11)
//      val postalCodeRegex = elems(14)
      val geoNameId = if(elems(16).nonEmpty) Some(elems(16).toInt) else None
      val neighbours = if(elems.length == 18) elems(17).split(",") else Array[String]()
      val equivalentFipsCode = if(elems.length == 19) Some(elems(18)) else None

      val country = Country(elems(4), elems(0), elems(1), elems(2), elems(3), elems(5), elems(6).toDouble, elems(7).toInt, continent, elems(9), elems(10), currencyName, elems(12), elems(13), elems(15).split(","), geoNameId, neighbours, equivalentFipsCode)
      countries = country :: countries
      continents.add(continent)
      currencies.add(currencyName)
    }
  }
}
