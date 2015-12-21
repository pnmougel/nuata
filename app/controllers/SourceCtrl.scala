package controllers

import com.github.tototoshi.play2.json4s.jackson.Json4s
import models.{SourceModel, FactModel}
import org.json4s.DefaultFormats
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import repositories.{SourceRepository, FactRepository}

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by nico on 12/11/15.
 */
class SourceCtrl extends Controller with Json4s {
  def index = Action.async(json) { implicit request =>
    implicit val formats = DefaultFormats
    val sources = request.body.values match {
      case x: List[_] => request.body.extract[List[SourceModel]]
      case x: Map[_, _] => List(request.body.extract[SourceModel])
    }
    SourceRepository.indexSources(sources).map(id => Ok(Json.obj("_id" -> id)) )
  }
}
