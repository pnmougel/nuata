package controllers

import java.util

import com.sksamuel.elastic4s.ElasticDsl.count
import com.sksamuel.elastic4s.jackson.ElasticJackson.Implicits._
import elasticsearch.ElasticSearch
import play.api.libs.json.Json
import play.api.mvc._
import play.api.mvc.{Action, Controller}
import repositories.{UnitRepository, OoiRepository, DimensionRepository}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConversions._
import scala.collection._

/**
 * Created by nico on 02/11/15.
 */
class AutoComplete extends Controller {
  def autoComplete(lastWord: String) = Action.async {
    val repositories = List(DimensionRepository, OoiRepository, UnitRepository)
    Future.sequence(repositories.map(repository => {
      repository.nameStartingWith(lastWord).map(response => {
        for (hit <- response.getHits.hits()) yield {
          val mapOfNames = hit.getSource.get("names").asInstanceOf[java.util.HashMap[String, util.ArrayList[String]]]

          val nameSet = mutable.HashSet[String]()
          for((lang, names) <- mapOfNames; name <- names) { nameSet.add(name) }

          Json.obj("en" -> Json.toJson(nameSet.toList))
        }
      })
    })).map( seqOfJson => Ok(Json.toJson(seqOfJson.flatten)))
  }
}
