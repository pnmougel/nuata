package geonames

import java.io.{File, PrintWriter}

import querybuilder.{Dimension, LocalizedString, Lang, Query}

import scala.collection.mutable
import scala.io.Source

/**
 * Created by nico on 18/11/15.
 */
object Builder {
  val query = new Query()

  // Create units
  val squareMeterUnit = query.addUnit(Lang.en("square meter"))
  val personUnit = query.addUnit(Lang.en("person"))
  val degreeUnit = query.addUnit(Lang.en("degree"))
  val meterUnit = query.addUnit(Lang.en("meter"))

  // Create objects of interests
  val areaOOI = query.addOOI(Lang.en("Area") ::: Lang.fr("Surface", "Superficie"), Lang.en("Surface of an area"), units = List(squareMeterUnit))
  val populationOOI = query.addOOI(Lang.en("Population") ::: Lang.fr("Population"), Lang.en("A number of person"), units = List(personUnit))
  val latitudeOOI = query.addOOI(Lang.en("Latitude") ::: Lang.fr("Latitude"), Lang.en("Geodesic latitude"), units = List(degreeUnit))
  val longitudeOOI = query.addOOI(Lang.en("Longitude") ::: Lang.fr("Longitude"), Lang.en("Geodesic longitude"), units = List(degreeUnit))
  val elevationOOI = query.addOOI(Lang.en("Elevation", "Height") ::: Lang.fr("Elevation", "Altitude", "Hauteur"), Lang.en("Height above sea level"), units = List(meterUnit))

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

  val continentCodeToName = Map[String, Dimension](
    "AF" -> query.addDimension(Lang.en("Africa") ::: Lang.fr("Afrique"), categories = List(areaInMapCategory, continentCategory)),
    "OC" -> query.addDimension(Lang.en("Australia") ::: Lang.fr("Océanie"), categories = List(areaInMapCategory, continentCategory)),
    "EU" -> query.addDimension(Lang.en("Europe") ::: Lang.fr("Europe"), categories = List(areaInMapCategory, continentCategory)),
    "AN" -> query.addDimension(Lang.en("Antartica") ::: Lang.fr("Antartique"), categories = List(areaInMapCategory, continentCategory)),
    "SA" -> query.addDimension(Lang.en("South America") ::: Lang.fr("Amérique du sud"), categories = List(areaInMapCategory, continentCategory)),
    "NA" -> query.addDimension(Lang.en("North America") ::: Lang.fr("Amérique du nord"), categories = List(areaInMapCategory, continentCategory)),
    "AS" -> query.addDimension(Lang.en("Asia") ::: Lang.fr("Asie"), categories = List(areaInMapCategory, continentCategory)))

  // Add the countries
  Country.read()
  var countryIdToDimension = mutable.HashMap[String, Dimension]()
  for((countryId, country) <- Country.countryCodeToCountry) {
    val localizedNames = GeoNames.getAlternateNames(country.geonameId, LocalizedString(country.name, "en"))
    val continent = continentCodeToName(country.continent)
    val countryDimension = query.addDimension(localizedNames, categories = List(areaInMapCategory, countryCategory), parents = List(continent))
    countryIdToDimension(countryId) = countryDimension

    query.addFact(country.population, List(countryDimension), populationOOI, None)
    query.addFact(country.area, List(countryDimension), areaOOI, None)
  }

  for ((line, i) <- Source.fromFile("/home/nico/data/geonames/cities1000.txt").getLines().zipWithIndex) {
    val city = GeoNames.readGeonameEntry(line)
    val country = countryIdToDimension(city.countryCode)
    val alternateNames = GeoNames.getAlternateNames(city)

    val cityDimension = query.addDimension(alternateNames, categories = List(areaInMapCategory, cityCategory), parents = List(country))

    for(population <- city.population) {
      query.addFact(population, List(cityDimension), populationOOI, None)
    }
    for(elevation <- city.elevation) {
      query.addFact(elevation, List(cityDimension), elevationOOI, None)
    }

    query.addFact(city.latitude, List(cityDimension), latitudeOOI, None)
    query.addFact(city.longitude, List(cityDimension), longitudeOOI, None)
  }

  def main(args: Array[String]) = {
    query.send()
  }
}
