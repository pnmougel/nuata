package controllers

import com.github.tototoshi.play2.json4s.jackson.Json4s
import models.{FactModel, CategoryModel}
import org.json4s.DefaultFormats
import play.api.libs.json.Json
import play.api.mvc.{Controller, Action}
import repositories.FactRepository
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

/**
 * Created by nico on 12/11/15.
 */
class FactCtrl extends Controller with Json4s {


  def index = Action.async(json) { implicit request =>
    implicit val formats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all
//    println(request.body)
    val facts = request.body.values match {
      case x: List[_] => request.body.extract[List[FactModel]]
      case x: Map[_, _] => List(request.body.extract[FactModel])
    }
//    println(facts)
//    request.body.extract[FactModel]
//    println("fact")
//    println(fact)
//    println(fact.at)
    FactRepository.indexFacts(facts).map(id => Ok(Json.obj("_id" -> id)) )
  }
}
