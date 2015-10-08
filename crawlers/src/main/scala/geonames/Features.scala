package geonames

import services.Categories

import scala.collection.mutable
import scala.io.Source

/**
 * Created by nico on 02/10/15.
 */


case class Feature(featureClass: String, code: String, name: String, description: Option[String])

object Features {
  val features = mutable.HashMap[(String, String), Feature]()
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

      features((featureClass, featureCode)) = Feature(featureClass, featureCode, featureName, featureDescription)
    }
    features(("A", "")) = Feature("A", "", "country, state, region,...", None)
    features(("H", "")) = Feature("H", "", "stream, lake, ...", None)
    features(("L", "")) = Feature("L", "", "parks,area, ...", None)
    features(("P", "")) = Feature("P", "", "city, village,...", None)
    features(("R", "")) = Feature("R", "", "road, railroad", None)
    features(("S", "")) = Feature("S", "", "spot, building, farm", None)
    features(("T", "")) = Feature("T", "", "mountain,hill,rock,...", None)
    features(("U", "")) = Feature("U", "", "undersea", None)
    features(("V", "")) = Feature("V", "", "forest,heath,...", None)
    features(("", "")) = Feature("", "", "Unknown", None)
  }
}
