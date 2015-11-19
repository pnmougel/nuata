package geonames


import java.io._

import querybuilder.LocalizedString

import scala.collection.mutable
import scala.io.Source

object GeoNames {
  val featureClassWithMissingFeature = mutable.HashMap[String, Int]()

  def readGeonameEntry(line: String) : GeonameEntry = {
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

    GeonameEntry(id, name, asciiName, alternateNames, latitude, longitude, featureClass, featureCode, countryCode, cc2,
      adminCode1, adminCode2, adminCode3, adminCode4, population, elevation, dem, timezone, modificationDate)
  }

  def getAlternateNames(geoNameId: Option[Int], curName: LocalizedString) : List[LocalizedString] = {
    val localizedName = geoNameId.map( id => GeoNames.alternateNames.getOrElse(id, scala.collection.mutable.HashMap[String, List[String]]())).getOrElse(scala.collection.mutable.HashMap[String, List[String]]())
    var localizedNames = for((lang, localizedNames) <- localizedName.toList; name <- localizedNames) yield { LocalizedString(name, lang) }
    if(!localizedNames.contains(curName)) {
      localizedNames = curName :: localizedNames
    }
    localizedNames
  }

  def getAlternateNames(entry: GeonameEntry) : List[LocalizedString] = {
    getAlternateNames(Some(entry.id), LocalizedString(entry.name, "en"))
  }

  lazy val alternateNames: mutable.HashMap[Int, Map[String, List[String]]] = {
    val res = mutable.HashMap[Int, Map[String, List[String]]]()
    for (line <- Source.fromFile("/home/nico/IdeaProjects/nuata/alternateNamesFast").getLines()) {
      val elems = line.split("\t")
      val geoNameId = elems(0).toInt
      val names = Map((for((elem, i) <- elems.zipWithIndex; if(i != 0)) yield {
        val a = elem.split("!!")
        val lang = a(0)
        val localizedNames = a(1).split("::")
        lang -> localizedNames.toList
      }) :_*)
      res(geoNameId) = names
    }
    res
  }

  def readAlternateNames = {
    val alternateNames = mutable.HashMap[Int, mutable.HashMap[String, List[String]]]()
    for (line <- Source.fromFile("/home/nico/data/geonames/alternateNames.txt").getLines()) {
      val elems = line.split("\t")
      val geonameId = elems(1).toInt
      val langCode = elems(2)
      val name = elems(3)
      if(langCode == "en" || langCode == "fr") {
        if(!name.contains(",") && !name.contains("(")) {
          val names = alternateNames.getOrElseUpdate(geonameId, mutable.HashMap[String, List[String]]())
          names(langCode) = name :: names.getOrElse(langCode, List[String]())
        }
      }
    }
    val alternateNamesFast = new PrintWriter(new File("alternateNamesFast"))
    for((geoNameId, names) <- alternateNames) {
      val localizedNames = (for((lang, localNames) <- names) yield {
        s"${lang}!!${localNames.mkString("::")}"
      }).mkString("\t")
      alternateNamesFast.println(s"$geoNameId\t$localizedNames")
    }
    alternateNamesFast.flush()
    alternateNamesFast.close()
  }

  def main(args: Array[String]) : Unit = {
//    readFast
//    readAlternateNames
//    readFeatures()
//    readCountries()
    Timer.printTimers()
  }
}

