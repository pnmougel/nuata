package controllers

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import elasticsearch.ElasticSearch
import org.json4s.ext.EnumSerializer
import models._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class Foo(names: Map[String, List[String]])

/**
 * Created by nico on 13/10/15.
 */
class Update extends Controller {
  import org.json4s._

  implicit val formats = DefaultFormats + new EnumSerializer(CreateOption)
  implicit val searchResultFormat = Json.writes[SearchResult]

  def update = Action.async { implicit rs =>
    val jsBody = rs.body.asJson.get.toString()
    val js = org.json4s.jackson.JsonMethods.parse(jsBody)
    val query = js.extract[CreateQuery]

    val queryItems = List("categories" -> query.categories, "dimensions" -> query.dimensions, "units" -> query.units, "oois" -> query.oois)
    val outJson = for((name, items) <- queryItems) yield {
      Future.sequence(for(item <- items) yield {
        item.searchItem().map( searchResult => { Json.toJson(searchResult) })
      }).map( json => Json.obj(name -> json))
    }
    val jsonForFacts = for(fact <- query.facts) yield {
      fact.searchItem().map( searchResult => { Json.toJson(searchResult) })
    }

    Future.sequence(outJson).map { responses => {
      var jsRes = Json.obj()
      for(r <- responses) {
        jsRes = jsRes ++ r
      }
      Ok(Json.toJson(jsRes))
    }}
  }

  def test = Action.async { implicit rs =>

    // Exact match query
    var foo = Map[String, List[String]]("en" -> List("Else", "Continent"), "fr" -> List("Continent"))

    val filteredQueries = for((lang, names) <- foo) yield {
      val filter = termsFilter(s"names.${lang}.raw", names:_*)
      filteredQuery filter(filter)
    }
    val q = search in "nuata" / "category" query { bool { should { filteredQueries } must { termQuery("names.en.raw" -> "Continent2") } } }

    /*
    val category = new Category(id = None, ref = "xx", names = Map("en" -> List("foo", "bar"), "fr" -> List()), descriptions = Map(), create = None)
    category.createItem().map( res => {
      println(res)
    })
    */

    ElasticSearch.client.execute { q }.map( res => {
      println(res)
    })
    /*
    val jsBody = rs.body.asJson.get.toString()
    val js = org.json4s.jackson.JsonMethods.parse(jsBody)


    val foo = js.extract[Foo]
    println(foo.names)
    */
    Future.successful(Ok("Cool"))
  }
}
