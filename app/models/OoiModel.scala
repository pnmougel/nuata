package models

import _root_.repositories.UnitRepository
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by nico on 02/11/15.
 */
case class OoiModel(_id: Option[String], _score: Option[Double], names: Map[String, List[String]], descriptions: Map[String, String], unitIds: List[String])
  extends LocalizedNamedModel(_id, _score, names, descriptions) {

  val units = Future.sequence(for(unitId <- unitIds) yield { UnitRepository.byId(unitId) })

  override def getIndexQuery() = {
    defaultIndexQuery ++ Map("unitIds" -> unitIds)
  }
  override def getSearchQuery() = defaultSearchQuery
  override def getMatchQuery() = defaultMatchQuery

  def isPerfectMatch(res: LocalizedNamedModel): Boolean = {
    true
  }

  def toJson : Future[JsObject] = {
    units.flatMap( items => { Future.sequence(items.map( item => { item.toJson.map(itemJson => itemJson) })) }).map( items => {
      Json.obj(
        "names" -> names,
        "descriptions" -> descriptions,
        "units" -> Json.toJson(items)
      )
    })
  }
}
