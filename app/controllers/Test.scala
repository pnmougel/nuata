package controllers

import com.sksamuel.elastic4s.ElasticDsl._
import elasticsearch.ElasticSearch
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import repositories.FactRepository
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by nico on 13/11/15.
 */
class Test extends Controller {
  //  AVEAj_3csFQ7gjHpy0OX
  def test = Action.async {
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
