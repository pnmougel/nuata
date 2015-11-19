package geonames

import querybuilder.{Dimension, LocalizedString, Query, Lang}

import scala.collection.mutable
import scala.io.Source
import scala.util.Random

/**
 * Created by nico on 02/10/15.
 */

object Country {
  case class Country(name: String, iso: String, iso3: String, isoNumeric: String, fips: String,
                     capital: String, area: Double, population: Int, continent: String, tld: String,
                     currencyCode: String, currencyName: String, phone: String, postalCodeFormat: String,
                     languages: Array[String], geonameId: Option[Int], neighbours: Array[String],
                     equivalentFipsCode: Option[String])

  val continentCodeToName = Map[String, List[LocalizedString]](
    "AF" -> (Lang.en("Africa") ::: Lang.fr("Afrique")),
    "OC" -> (Lang.en("Australia") ::: Lang.fr("Océanie")),
    "EU" -> (Lang.en("Europe") ::: Lang.fr("Europe")),
    "AN" -> (Lang.en("Antartica") ::: Lang.fr("Antartique")),
    "SA" -> (Lang.en("South America") ::: Lang.fr("Amérique du sud")),
    "NA" -> (Lang.en("North America") ::: Lang.fr("Amérique du nord")),
    "AS" -> (Lang.en("Asia") ::: Lang.fr("Asie")))


  def main(args: Array[String]) = {
    read()
    insert()
  }

  val continents = mutable.HashSet[String]()
  val currencies = mutable.HashSet[String]()
  var countries = List[Country]()

  def insert() = {
    val query = new Query()

    // Create categories
    val areaInMapCategory = query.addCategory(
      Lang.en("Area in a map") ::: Lang.fr("Zone géographique"),
      Lang.en("Something that can be displayed in a map"))
    val continentCategory = query.addCategory(
      Lang.en("Continent") ::: Lang.fr("Continent"),
      Lang.en("A continent is one of several very large landmasses on Earth. This category is based on the model with 7 continents"))
    val countryCategory = query.addCategory(Lang.en("Country") ::: Lang.fr("Pays"))
    val cityCategory = query.addCategory(Lang.en("City") ::: Lang.fr("Ville"))
    val capitalCategory = query.addCategory(
      Lang.en("Capital") ::: Lang.fr("Capitale"),
      Lang.en("The area of a country, province, region, or state, regarded as enjoying primary status, usually but not always the seat of the government"))
    val currencyCategory = query.addCategory(
      Lang.en("Currency") ::: Lang.fr("Monnaie"),
      Lang.en("A system of money (monetary units) in common use, especially in a nation"))

    // Create objects of interests
    val squareMeterUnit = query.addUnit(querybuilder.Lang.en("square meter"))
    val personUnit = query.addUnit(querybuilder.Lang.en("person"))

    val areaOOI = query.addOOI(
      querybuilder.Lang.en("Area") ::: querybuilder.Lang.fr("Surface", "Superficie"),
      querybuilder.Lang.en("Surface of an area"),
      units = List(squareMeterUnit))

    val populationOOI = query.addOOI(
      querybuilder.Lang.en("Population"),
      querybuilder.Lang.en("A number of person"),
      units = List(personUnit))

    // Create continents
    for(continent <- continents) {
      query.addDimension(continentCodeToName(continent), categories = List(areaInMapCategory, continentCategory))
    }

    // Create currencies
    for(currency <- currencies; if currency.nonEmpty) {
      query.addDimension(Lang.en(currency), categories = List(currencyCategory))
    }

    // Create countries and their capitals
    for(country <- countries) {
      if(country.name.nonEmpty) {
        val countryDimension = query.addDimension(Lang.en(country.name), categories = List(areaInMapCategory, countryCategory))
        query.addFact(country.area, List(countryDimension), areaOOI, None)
        query.addFact(country.population, List(countryDimension), populationOOI, None)
      }

      if(country.capital.nonEmpty) {
        query.addDimension(Lang.en(country.capital), categories = List(areaInMapCategory, cityCategory, capitalCategory))
      }
    }
  }

  val countryCodeToNames = scala.collection.mutable.HashMap[String, (List[LocalizedString], String)]()
  val countryCodeToCountry = scala.collection.mutable.HashMap[String, Country]()

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

      val localizedNames = GeoNames.getAlternateNames(geoNameId, LocalizedString(country.name, "en"))
      countryCodeToNames(elems(0)) = (localizedNames, continent)
      countryCodeToCountry(elems(0)) = country

      countries = country :: countries
      continents.add(continent)
      currencies.add(currencyName)
    }
  }
}
