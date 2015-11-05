package controllers

import com.github.tototoshi.play2.json4s.jackson.Json4s
import models._
import org.json4s
import org.json4s.ext.EnumSerializer
import org.json4s.{DefaultFormats, Extraction}
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{Action, Controller, Request}
import queries.CreateOption
import repositories._
import repositories.commons.LocalizedNamedItemRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by nico on 05/11/15.
 */
class CrudCtrl extends Controller with Json4s {
  implicit val formats = DefaultFormats + new EnumSerializer(CreateOption)

  def getItem(model: String, request: Request[json4s.JValue]) = {
    model match {
      case "category" => request.body.extract[CategoryModel]
      case "dimension" => request.body.extract[DimensionModel]
      case "unit" => request.body.extract[UnitModel]
      case "ooi" => request.body.extract[OoiModel]
    }
  }

  def getRepository(model: String) = {
    model match {
      case "category" => CategoryRepository
      case "dimension" => DimensionRepository
      case "unit" => UnitRepository
      case "ooi" => OoiRepository
      case "fact" => FactRepository
    }
  }

  val validModels = Set("category", "dimension", "unit", "ooi")

  def index(model: String) = Action.async(json) { implicit rs =>
    if(validModels.contains(model)) {
      val item = getItem(model, rs)
      getRepository(model).indexItem(item).map( id => Ok(Json.obj("_id" -> id)) )
    } else {
      Future.successful(Status(401)(Json.obj("error" -> s"Invalid parameter: $model")))
    }
  }

  def find(model: String) = Action.async(json) { implicit rs =>
    if(validModels.contains(model)) {
      val item = getItem(model, rs)
      getRepository(model).searchExact(item).map( items =>
        Ok(Extraction.decompose(items))
      )
    } else {
      Future.successful(Status(401)(Json.obj("error" -> s"Invalid parameter: $model")))
    }
  }

  def findMatch(model: String) = Action.async(json) { implicit rs =>
    if(validModels.contains(model)) {
      val item = getItem(model, rs)
      getRepository(model).searchMatch(item).map( items =>
        Ok(Extraction.decompose(items))
      )
    } else {
      Future.successful(Status(401)(Json.obj("error" -> s"Invalid parameter: $model")))
    }
  }

  def update(model: String) = Action.async(json) { implicit rs =>
    if(validModels.contains(model)) {
      val item = getItem(model, rs)
      getRepository(model).indexItem(item).map( id => Ok(Json.obj("id" -> id)) )
    } else {
      Future.successful(Status(401)(Json.obj("error" -> s"Invalid parameter: $model")))
    }
  }
}
