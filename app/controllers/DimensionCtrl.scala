package controllers

import com.github.tototoshi.play2.json4s.jackson.Json4s
import elasticsearch.ElasticSearch
import models.{DimensionModel, CategoryModel}
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.{Controller, Action}
import repositories.{SearchOptions, NameOperations, DimensionRepository}
import scala.concurrent.ExecutionContext.Implicits.global
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.jackson.ElasticJackson.Implicits._
import scala.concurrent.Future
/**
 * Created by nico on 07/12/15.
 */
class DimensionCtrl extends Controller with Json4s {
  implicit val formats = DefaultFormats

  def removeCategory(dimensionId: String, categoryId: String) = Action.async { implicit rs =>
    DimensionRepository.removeDependency(dimensionId, categoryId, "categoryIds").map( res => {
      Ok("")
    })
  }

  def removeParent(dimensionId: String, parentId: String) = Action.async { implicit rs =>
    DimensionRepository.removeDependency(dimensionId, parentId, "parentIds").map( res => {
      Ok("")
    })
  }

  def findByName(name: String, start: Int, limit: Int, categoryIds: List[String], parentIds: List[String]) = Action.async { implicit rs =>

    val searchOptions = SearchOptions(name, NameOperations.StartsWith, start, limit,
      Map("categoryIds" -> categoryIds, "parentIds" -> parentIds))
    DimensionRepository.doSearchWithMapping(searchOptions).map( item => {
      Ok(Extraction.decompose(item))
    })
  }

  def getChildrenRecur(ids: List[String], maxLevel: Int = 0, curLevel: Int) : Future[List[Array[DimensionModel]]] = {
    var allIds = List[Array[DimensionModel]]()
    DimensionRepository.join("parentIds", ids).flatMap( res => {
      val dimensions = res.as[DimensionModel]
      val allNewIds = dimensions.map(_._id.get)
      allIds = dimensions :: allIds
      val newIds = allNewIds.take(1000)
      if(allNewIds.isEmpty || curLevel >= maxLevel) {
        Future(List())
      } else {
        Future.sequence(allNewIds.sliding(1000).toList.map( idSlice => {
          getChildrenRecur(newIds.toList, maxLevel, curLevel + 1).map( l => {
            allIds = l ::: allIds
            allIds
          })
        })).map( it => {
          it.flatten
        })
      }
    })
  }

  def updateItem(id: String) = Action.async(json) { request =>
    val dimension = request.body.extract[DimensionModel]
    ElasticSearch.client.execute(update(id).in("nuata/dimension").doc("name" -> dimension.name)).map( res => {
      Ok(res.getId)
    })
//    Future.successful(Ok(""))
  }

  def getChildren(id: String, maxLevel: Int = 100) = Action.async { implicit rs =>
    getChildrenRecur(List(id), maxLevel, 1).map( res => {
      res.flatten.filter( dimension => {
        dimension.categoryIds.contains("4521568")
      })
//      Extraction.decompose(res.flatten.toList)
      Ok(Extraction.decompose(res.flatten.toList))
    })
  }
}
