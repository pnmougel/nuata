package insee.crawl

import java.io.File

import insee.crawl.NivGeo.NivGeo
import insee.crawl.Themes.{SubTheme, Theme}
import org.jsoup.Jsoup
import play.api.libs.json.Json
import scala.collection.JavaConversions._
import scala.io.Source

/**
 * Created by nico on 10/10/15.
 */
object Datasets {
  val nbElementsPerPage = 100

  case class Dataset(name: String, url: String, kind: String)
  implicit val jsonFormat = Json.format[Dataset]

  def load(theme: Theme, subTheme: SubTheme, nivGeo: NivGeo) = {
    val filePath = s"${Utils.baseStructurePath}/datasets/${theme.id}/${subTheme.id}/${nivGeo.id}/datasets.json"
    val file = new File(filePath)
    if(file.exists()) {
      val data = Source.fromFile(file).getLines().mkString("\n")
      Json.parse(data).as[List[Dataset]]
    } else {
      // Retrieve the data
      val themes = getDatasets(theme, subTheme, nivGeo).toList
      val json = Json.toJson(themes)
      Utils.withWriter(file,  bw => {
        bw.print(Json.prettyPrint(json))
      })
      themes
    }
  }

  private def getDatasets(theme: Theme, subTheme: SubTheme, nivGeo: NivGeo) : Seq[Dataset] = {
    // Get the number of documents
    val url = s"/fr/themes/theme.asp?theme=${theme.id}&sous_theme=${subTheme.id}&nivgeo=${nivGeo.id}&type=&produit=OK"
    val doc = Jsoup.parse(Utils.getPage(url))
    var documents = Vector[Dataset]()
    val pagination = doc.getElementsByClass("pagination")
    if(pagination.size() != 0) {
      val elem = doc.getElementsByClass("pagination")(0)
      val nbElements = elem.getElementsByTag("p")(0).ownText().split(" sur ")(1).toInt
      val nbPages = Math.ceil(nbElements.toDouble / nbElementsPerPage).toInt
      for(pageNum <- 1 to nbPages) {
        documents ++= loadPage(theme, subTheme, nivGeo, pageNum)
      }
    }
    documents
  }

  private def loadPage(theme: Theme, subTheme: SubTheme, nivGeo: NivGeo, pageNum: Int) : Vector[Dataset] = {
    val url = s"/fr/themes/theme.asp?theme=${theme.id}&sous_theme=${subTheme.id}&nivgeo=${nivGeo.id}&type=&produit=OK&numpage=${pageNum}&nombre=100"
    val doc = Jsoup.parse(Utils.getPage(url))
    val publications = doc.getElementById("publications")
    (for(publication <- publications.getElementsByTag("li")) yield {
      val kind = publication.attr("class")
      val link = publication.getElementsByTag("a")(0)
      val name = link.ownText()
      val url = link.attr("href")
      Dataset(name, url, kind)
    }).toVector
  }
}
