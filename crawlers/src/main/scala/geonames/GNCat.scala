package geonames

import services.{Categories, Lang}

/**
 * Created by nico on 07/10/15.
 */
object GNCat{
  val areaInMapCategory = Categories.registerCategory(Seq(Lang.en("Area in a map"), Lang.fr("Zone géographique")), Some("Something that can be displayed in a map"))
  val continentCategory = Categories.registerCategory(Seq(Lang.en("Continent"), Lang.fr("Continent")), Some("A continent is one of several very large landmasses on Earth. This category is based on the model with 7 continents"))
  val countryCategory = Categories.registerCategory(Seq(Lang.en("Country"), Lang.fr("Pays")))
  val cityCategory = Categories.registerCategory(Seq(Lang.en("City"), Lang.fr("Ville")))
  val capitalCategory = Categories.registerCategory(Seq(Lang.en("Capital"), Lang.fr("Capitale")), Some("The area of a country, province, region, or state, regarded as enjoying primary status, usually but not always the seat of the government"))
  val currencyCategory = Categories.registerCategory(Seq(Lang.en("Currency"), Lang.fr("Monnaie")), Some("A system of money (monetary units) in common use, especially in a nation"))
  Categories.update()
}
