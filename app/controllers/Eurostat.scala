package controllers

import java.io.{FileReader, File}

import com.github.tototoshi.play2.json4s.jackson.Json4s
import com.sksamuel.elastic4s.ElasticDsl._
import elasticsearch.ElasticSearch
import org.json4s.jackson.Serialization._
import org.json4s.{Extraction, DefaultFormats}
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future
import scala.io.Source
import org.json4s._
import org.json4s.jackson.JsonMethods._

/**
 * Created by nico on 07/12/15.
 */
class Eurostat extends Controller with Json4s {
  implicit val formats = DefaultFormats

  def getCategories = Action.async { rs =>
    val data = Source.fromFile("/home/nico/data/eurostat/categories.json").getLines().mkString("\n")
    val js = org.json4s.jackson.JsonMethods.parse(data)
    println(js)
//    val js = Extraction.decompose(Map("yo" -> "dsqsd"))

    Future.successful(Ok(js))
  }
}

object EurostatQuery {
  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val formats = DefaultFormats
  import com.sksamuel.elastic4s._
  import com.sksamuel.elastic4s.jackson.ElasticJackson.Implicits._

  case class DimensionDescription(code: String, names: Map[String, String])
  case class CategoryDescription(_id: String, code: String, names: Map[String, String], dimensions: List[DimensionDescription])

  def main(args: Array[String]) {
    val esQuery = search in "eurostat" / "categories" query "*"
    val res = ElasticSearch.client.execute { esQuery }.await
      for(hit <- res.hits) yield {
        println(hit.as[CategoryDescription])
      }
  }
}

object EurostatLoader {
  import com.sksamuel.elastic4s.ElasticDsl._

  def main(args: Array[String]) {
    implicit val formats = DefaultFormats
    val file = new File("/home/nico/data/eurostat/categories.json")
    ElasticSearch.client.execute { deleteIndex("eurostat") }.await

    case class CategoryWithDimension(names: Map[String, String], var dimensions: Map[String, Map[String, String]])
    val categories = read[Map[String, CategoryWithDimension]](new FileReader(file))

    for((categoryCode, category) <- categories) {
      val dimensions = for((dimensionCode, dimensionsNames) <- category.dimensions) yield {
        Map("code" -> dimensionCode, "names" -> dimensionsNames)
      }
      val categoryFields = Map("code" -> categoryCode, "names" -> category.names, "dimensions" -> dimensions)
      ElasticSearch.client.execute {
        index into "eurostat" / "categories" fields categoryFields
      }
    }
  }
}