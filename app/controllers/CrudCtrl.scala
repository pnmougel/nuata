package controllers

import com.github.tototoshi.play2.json4s.jackson.Json4s
import models._
import org.json4s
import org.json4s.ext.EnumSerializer
import org.json4s.{DefaultFormats, Extraction}
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{Action, Controller, Request}
import repositories._
import repositories.commons.LocalizedNamedItemRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by nico on 05/11/15.
 */
class CrudCtrl extends Controller with Json4s {
  implicit val formats = DefaultFormats

  def getItems(model: String, request: Request[json4s.JValue]) = {
    request.body.values match {
      case x: List[_] => {
        model match {
          case "category" => request.body.extract[List[CategoryModel]]
          case "dimension" => request.body.extract[List[DimensionModel]]
          case "unit" => request.body.extract[List[UnitModel]]
          case "ooi" => request.body.extract[List[OoiModel]]
        }
      }
      case x: Map[_, _] => {
        List(model match {
          case "category" => request.body.extract[CategoryModel]
          case "dimension" => request.body.extract[DimensionModel]
          case "unit" => request.body.extract[UnitModel]
          case "ooi" => request.body.extract[OoiModel]
        })
      }
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
      val items = getItems(model, rs)
      getRepository(model).indexItems(items).map( id => Ok(Json.obj("_id" -> id)) )
    } else {
      Future.successful(Status(401)(Json.obj("error" -> s"Invalid parameter: $model")))
    }
  }

  def find(model: String) = Action.async(json) { implicit rs =>
    if(validModels.contains(model)) {

//      val item = rs.body.values match {
//        case x: List[_] => rs.body.extract[List[CategoryModel]]
//        case x: Map[_, _] => List(rs.body.extract[CategoryModel])
//      }

//      val item = rs.body.extract[List[CategoryModel]]
//      println(item)
//      val item = getItem(model, rs)

      val items = getItems(model, rs)
      getRepository(model).searchExacts(items).map( items =>
        Ok(Extraction.decompose(items))
      )
    } else {
      Future.successful(Status(401)(Json.obj("error" -> s"Invalid parameter: $model")))
    }
  }

  def findMatch(model: String) = Action.async(json) { implicit rs =>
    if(validModels.contains(model)) {
      val items = getItems(model, rs)
      getRepository(model).searchMatches(items).map( items =>
        Ok(Extraction.decompose(items))
      )
    } else {
      Future.successful(Status(401)(Json.obj("error" -> s"Invalid parameter: $model")))
    }
  }

  def byId(model: String, id: String) = Action.async { implicit rs =>
    if(validModels.contains(model)) {
      getRepository(model).byIdOpt(id).map( item => {
        if(item.isDefined) {
          Ok(Extraction.decompose(item))
        } else {
          Ok(Json.obj("error" -> s"Missing $model with id '$id'"))
        }
      })
    } else {
      Future.successful(Status(401)(Json.obj("error" -> s"Invalid parameter: $model")))
    }
  }

  def update(model: String) = Action.async(json) { implicit rs =>
    if(validModels.contains(model)) {
      val items = getItems(model, rs)
      getRepository(model).indexItems(items).map( id => Ok(Json.obj("id" -> id)) )
    } else {
      Future.successful(Status(401)(Json.obj("error" -> s"Invalid parameter: $model")))
    }
  }
}
