package insee.crawl

import java.io.{PrintWriter, File}

import org.jsoup.Jsoup
import play.api.libs.json.Json
import scala.collection.JavaConversions._
import scala.io.Source

/**
 * Created by nico on 10/10/15.
 */
object Themes {
  case class SubTheme(name: String, url: String, id: Int)
  case class Theme(name: String, url: String, id: Int, subThemes : List[SubTheme])
  implicit val jsonThemeFormat = Json.format[SubTheme]
  implicit val jsonSubthemeFormat = Json.format[Theme]

  def loadThemes() : List[Theme] = {
    val file = new File(Utils.baseStructurePath + "/themes.json")
    if(file.exists()) {
      val data = Source.fromFile(file).getLines().mkString("\n")
      Json.parse(data).as[List[Theme]]
    } else {
      // Retrieve the data
      val themes = getThemes().toList
      val json = Json.toJson(themes)
      Utils.withWriter(file,  bw => {
        bw.print(Json.prettyPrint(json))
      })
      themes
    }
  }

  def getThemes(): Seq[Theme] = {
    val doc = Jsoup.parse(Utils.getPage("/fr/themes/"))
    val elems = doc.getElementById("navigation-gauche")
    val links = elems.getElementsByTag("a")
    for(link <- links) yield {
      val url = link.attr("href")
      val id = url.split("=")(1).toInt

      val subthemes = getSubThemes(url)

      Theme(link.attr("title"), url, id, subthemes)
    }
  }

  def getSubThemes(url: String) : List[SubTheme] = {
    val doc = Jsoup.parse(Utils.getPage(url))
    val elem = doc.getElementById("navigation-gauche")
    val links = elem.getElementsByClass("parent")(0).getElementsByTag("a")
    (for(link <- links) yield {
      val url = link.attr("href")
      val id = url.split("=")(2).toInt
      new SubTheme(link.attr("title"), url, id)
    }).toList
  }
}
