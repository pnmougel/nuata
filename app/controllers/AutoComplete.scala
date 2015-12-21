package controllers

import java.util

import com.fasterxml.jackson.databind.JsonSerializable
import com.github.tototoshi.play2.json4s.jackson.Json4s
import com.sksamuel.elastic4s.ElasticDsl.count
import com.sksamuel.elastic4s.jackson.ElasticJackson.Implicits._
import elasticsearch.ElasticSearch
import models.{LocalizedNamedModel, DimensionModel}
import org.json4s.JsonAST.JObject
import org.json4s.{DefaultFormats, Extraction}
import play.api.libs.json.Json
import play.api.mvc._
import play.api.mvc.{Action, Controller}
import repositories._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConversions._
import scala.collection._
import org.json4s.JsonDSL._
/**
 * Created by nico on 02/11/15.
 */
class AutoComplete extends Controller with Json4s {
  implicit val formats = DefaultFormats

  def autoComplete(lastWord: String) = Action.async {
    val repositories = List(DimensionRepository, OoiRepository, UnitRepository)
    val searchOptions = SearchOptions(lastWord, NameOperations.StartsWith, 0, 10)

    Future.sequence(repositories.map(repository => {

      repository.searchAndExpand(searchOptions).map( jsonValues => {
        (repository.`type`, Extraction.decompose(jsonValues))
      } )
    })).map( seqOfJson => {
      Ok(Extraction.decompose(seqOfJson.foldLeft(JObject()) { (value, acc) => value ~ acc }))
    })
  }
}
