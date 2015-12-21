package controllers

import com.github.tototoshi.play2.json4s.jackson.Json4s
import com.sksamuel.elastic4s.ElasticDsl.update
import models._
import org.json4s
import org.json4s.ext.EnumSerializer
import org.json4s.{DefaultFormats, Extraction}
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{Action, Controller, Request}
import repositories.DimensionRepository._
import repositories._
import repositories.commons.LocalizedNamedItemRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.jackson.ElasticJackson.Implicits._
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

  def search(model: String,
                       name: String, start: Int, limit: Int,
                       categoryIds: List[String], parentIds: List[String], unitIds: List[String],
                        operation: String, expand: Boolean) = Action.async { request =>
    val nameOperation = operation match {
      case "starts" => NameOperations.StartsWith
      case "match" => NameOperations.Match
      case "exact" => NameOperations.Exact
      case _ => NameOperations.StartsWith
    }
    if(validModels.contains(model)) {
      val searchOptions = SearchOptions(name, nameOperation, start, limit,
        Map("categoryIds" -> categoryIds, "parentIds" -> parentIds, "unitIds" -> unitIds))
      val repository = getRepository(model)
      if(expand) {
        repository.searchAndExpand(searchOptions).map( item => {
          Ok(Extraction.decompose(item))
        })
      } else {
        repository.doSearchWithMapping(searchOptions).map( item => {
          Ok(Extraction.decompose(item))
        })
      }
    } else {
      Future.successful(Status(401)(Json.obj("error" -> s"Invalid parameter: $model")))
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

  def findByName(model: String, name: String, start: Int, limit: Int) = Action.async { implicit rs =>
    val searchOptions = SearchOptions(name, NameOperations.StartsWith, start, limit)
    if(validModels.contains(model)) {
      getRepository(model).doSearchWithMapping(searchOptions).map( res => {
        Ok(Extraction.decompose(res))
      })
    } else {
      Future.successful(Status(401)(Json.obj("error" -> s"Invalid parameter: $model")))
    }
  }

  def setNames(model: String, itemId: String) = Action.async(json) { implicit rs =>
    val names = rs.body.extract[Map[String, List[String]]]
    if(validModels.contains(model)) {
      getRepository(model).byIdOpt(itemId).map( item => {
        if(item.isDefined) {
          client.execute {
            update id itemId in "nuata" / model docAsUpsert Map("names" -> names)
          }
          Ok("")
        } else {
          Ok(Json.obj("error" -> s"Missing $model with id '$itemId'"))
        }
      })
    } else {
      Future.successful(Status(401)(Json.obj("error" -> s"Invalid parameter: $model")))
    }
  }


  def setDescriptions(model: String, itemId: String) = Action.async(json) { implicit rs =>
    val descriptions = rs.body.extract[Map[String, String]]
    if(validModels.contains(model)) {
      getRepository(model).byIdOpt(itemId).map( item => {
        if(item.isDefined) {
          client.execute {
            update id itemId in "nuata" / model docAsUpsert Map("descriptions" -> descriptions)
          }
          Ok("")
        } else {
          Ok(Json.obj("error" -> s"Missing $model with id '$itemId'"))
        }
      })
    } else {
      Future.successful(Status(401)(Json.obj("error" -> s"Invalid parameter: $model")))
    }
  }
}
