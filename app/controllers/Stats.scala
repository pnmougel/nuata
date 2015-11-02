package controllers

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import elasticsearch.ElasticSearch
import org.json4s.ext.EnumSerializer
import models._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by nico on 30/10/15.
 */
class Stats extends Controller  {

  def countFacts = Action.async {
    ElasticSearch.client.execute { count from "nuata" types "fact" }.map( res => {
      Ok(Json.obj("nbFacts" -> res.getCount))
    })
  }
}
