package models

import repositories.{OoiRepository, DimensionRepository}
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by nico on 02/11/15.
 */
case class FactModel(_id: Option[String], _score: Option[Double],
                     ooiIds: List[String], dimensionIds: List[String],
                     at: Option[java.util.Date],
                     value: Option[Double], valueInt: Option[Long])
  extends BaseModel(_id, _score) {

  val oois = Future.sequence(for(ooiId <- ooiIds) yield { OoiRepository.byId(ooiId) })
  val dimensions = Future.sequence(for(dimensionId <- dimensionIds) yield { DimensionRepository.byId(dimensionId) })

  def toJson: Future[JsObject] = {
    dimensions.flatMap( items => { Future.sequence(items.map( item => { item.toJson.map(itemJson => itemJson) })) }).flatMap( dimensionsJson => {
      oois.flatMap( items => { Future.sequence(items.map( item => { item.toJson.map(itemJson => itemJson) })) }).map( ooisJson => {
        Json.obj(
          "value" -> value,
          "valueInt" -> valueInt,
          "dimensions" -> Json.toJson(dimensionsJson),
          "oois" -> Json.toJson(ooisJson)
        )
      })
    })
  }
}
