package controllers

import com.github.tototoshi.play2.json4s.jackson.Json4s
import com.sksamuel.elastic4s.ElasticDsl._
import elasticsearch.ElasticSearch
import org.json4s.JsonAST.JValue
import org.json4s.{DefaultFormats, Extraction}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by nico on 13/11/15.
 */
class Test extends Controller with Json4s {

  case class Foo(a: String, b: JValue)

  def getRawJson = Action.async(json) { implicit rs =>
    val z = Map("hh" -> Map("yo" -> "kk"))
    ElasticSearch.client.execute {
      index into "eurostat" / "test" fields z
    }
    implicit val formats = DefaultFormats
    val res = Foo("Test", rs.body)
    Future.successful(Ok(Extraction.decompose(res)))
  }
  //  AVEAj_3csFQ7gjHpy0OX
  def test(start: Int) = Action.async { request =>
    println(start)
    Future.successful(Ok(""))
    /*
    ElasticSearch.client.execute(search in "nuata" / "dimension" query {
      bool {
        must {
          List(
            nestedQuery("names").query( bool {
              should {
                filteredQuery filter termsFilter(s"names.en.raw", "Australia")
              }
            }),
            must(List(termQuery("categoryIds", "AVEAj_3csFQ7gjHpy0OW"), termQuery("categoryIds", "AVEAj_3csFQ7gjHpy0OX")))
          )
        }
      }
    }).map(x => {
      println(x)
      Ok(Json.obj("nbFacts" -> "fdf"))
    })
    */


//    ElasticSearch.client.execute(search in "nuata" / "dimension" query {
//      bool {
//        must {
//          nestedQuery("names").query( bool {
//            should {
//              filteredQuery filter termsFilter(s"names.en.raw", "Australia")
//            }
//          })
//        }
//        should(List(termQuery("categoryIds", "AVEAj_3csFQ7gjHpy0OW"), termQuery("categoryIds", "AVEAj_3csFQ7gjHpy0OX")))
//      }
//    }).map(x => {
//      println(x)
//      Ok(Json.obj("nbFacts" -> "fdf"))
//    })

//    ElasticSearch.client.execute(search in "nuata" / "dimension" query {
//      bool {
//        should(List(termQuery("categoryIds", "AVEAj_3csFQ7gjHpy0OW"), termQuery("categoryIds", "AVEAj_3csFQ7gjHpy0OX")))
//      }
//    }).map(x => {
//      println(x)
//      Ok(Json.obj("nbFacts" -> "fdf"))
//    })
  }

}
