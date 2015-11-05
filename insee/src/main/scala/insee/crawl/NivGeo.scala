package insee.crawl

import java.io.File

import org.jsoup.Jsoup
import play.api.libs.json.Json
import scala.collection.JavaConversions._
import scala.io.Source

/**
 * Created by nico on 10/10/15.
 */
object NivGeo {
  def basePage = "/fr/themes/theme.asp?theme=10&sous_theme=1"

  case class NivGeo(name: String, id: Int)
  implicit val jsonFormat = Json.format[NivGeo]

  def load() = {
    val file = new File(Utils.baseStructurePath + "/nivgeo.json")
    if(file.exists()) {
      val data = Source.fromFile(file).getLines().mkString("\n")
      Json.parse(data).as[List[NivGeo]]
    } else {
      // Retrieve the data
      val themes = getNivGeo().toList
      val json = Json.toJson(themes)
      Utils.withWriter(file,  bw => {
        bw.print(Json.prettyPrint(json))
      })
      themes
    }
  }

  def getNivGeo(): Seq[NivGeo] = {
    val doc = Jsoup.parse(Utils.getPage(basePage))
    val elems = doc.getElementsByClass("nivgeo")(0).getElementsByTag("li")
    for(elem <- elems) yield {
      val id = elem.getElementsByTag("input")(0).attr("value").toInt
      val name = elem.getElementsByTag("label")(0).ownText()
      NivGeo(name, id)
    }
  }
}
