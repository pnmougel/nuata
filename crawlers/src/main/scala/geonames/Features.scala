package geonames


import querybuilder.{Category, Lang}

import scala.collection.mutable
import scala.io.Source

/**
 * Created by nico on 02/10/15.
 */


//case class Feature(featureClass: String, code: String, name: String, description: Option[String])

object Features {
  val features = mutable.HashMap[(String, String), Category]()
  val featuresCodeToId = mutable.HashMap[(String, String), Long]()
  def read() = {
    for(line <- Source.fromFile("/home/nico/data/geonames/featureCodes_en.txt").getLines()) {
      val elems = line.split("\t")
      //      println(line)
      if(elems.size != 3 && elems.size != 2) {
        println("Invalid line " + line)
      }
      val (featureClass, featureCode) = if(elems(0).contains(".")) {
        (elems(0).split("\\.")(0), elems(0).split("\\.")(1))
      } else {
        (elems(0), "")
      }
      val featureName = elems(1)
      val featureDescription = if(elems.length > 2) Some(elems(2)) else None

      // Create a new category from the feature
      if(!featureName.isEmpty) {
        /*
        val res = Categories.createCategory(featureName, featureDescription)
        for(categoryId <- res.id) {
          featuresCodeToId((featureClass, featureCode)) = categoryId
        }
        if(!res.id.isDefined) {
          println(s"Several categories $featureName already exists")
        }
        */
      }
      val description = featureDescription.map( desc => Lang.en(desc)).getOrElse(List())
      val category = new Category(Lang.en(featureName), description)

      // features((featureClass, featureCode)) = Feature(featureClass, featureCode, featureName, featureDescription)
      features((featureClass, featureCode)) = category
    }
    features(("A", "")) = new Category(Lang.en("country, state, region,..."))
    features(("H", "")) = new Category(Lang.en("stream, lake, ..."))
    features(("L", "")) = new Category(Lang.en("parks,area, ..."))
    features(("P", "")) = new Category(Lang.en("city, village,..."))
    features(("R", "")) = new Category(Lang.en("road, railroad"))
    features(("S", "")) = new Category(Lang.en("spot, building, farm"))
    features(("T", "")) = new Category(Lang.en("mountain,hill,rock,..."))
    features(("U", "")) = new Category(Lang.en("undersea"))
    features(("V", "")) = new Category(Lang.en("forest,heath,..."))
    features(("", "")) = new Category(Lang.en("Unknown"))

//    features(("A", "")) = Feature("A", "", "country, state, region,...", None)
//    features(("H", "")) = Feature("H", "", "stream, lake, ...", None)
//    features(("L", "")) = Feature("L", "", "parks,area, ...", None)
//    features(("P", "")) = Feature("P", "", "city, village,...", None)
//    features(("R", "")) = Feature("R", "", "road, railroad", None)
//    features(("S", "")) = Feature("S", "", "spot, building, farm", None)
//    features(("T", "")) = Feature("T", "", "mountain,hill,rock,...", None)
//    features(("U", "")) = Feature("U", "", "undersea", None)
//    features(("V", "")) = Feature("V", "", "forest,heath,...", None)
//    features(("", "")) = Feature("", "", "Unknown", None)
  }
}
