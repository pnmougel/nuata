package insee.crawl


import insee.crawl.Datasets.Dataset
import org.jsoup.Jsoup
import play.api.libs.json.Json

import scala.collection.mutable
import scala.io.Source
import scalaj.http.Http
import scala.collection.JavaConversions._

/**
 * Created by nico on 02/10/15.
 *
 * Crawl the INSEE database
 */

object Crawler {

  def main(args: Array[String]) = {
    val nivGeo = NivGeo.load()
    val themes = Themes.loadThemes()

    var datasets = mutable.HashSet[Dataset]()

    for(theme <- themes) {
      println(s"Doing theme (${theme.id}) " + theme.name)
      for(subTheme <- theme.subThemes) {
        println(s"  Doing subTheme (${subTheme.id}) " + subTheme.name)
        for(niv <- nivGeo) {
          println(s"    Doing geo (${niv.id}) " + niv.name)
          for(dataset <- Datasets.load(theme, subTheme, niv)) {
            datasets.add(dataset)
          }
        }
      }
    }

    println(datasets.size)

//    Datasets.load(theme, subTheme, geo)

//    val themes = getThemes()
//    val json = Json.toJson(themes)
//    println(Json.prettyPrint(json))
//    for(theme <- getThemes()) {
//    }
//    Jsoup.parse(getPage("themes"))
  }

}
