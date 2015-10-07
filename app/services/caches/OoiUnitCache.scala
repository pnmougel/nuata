package services.caches

import play.api.db.DB
import anorm._
import anorm.SqlParser._
import play.api.Play.current
import scala.collection.mutable

/**
 * Created by nico on 07/10/15.
 */
object OoiUnitCache {
  var cache = mutable.HashMap[Long, String]()

  def buildCache() = DB.withConnection { implicit c =>
    val newCache = mutable.HashMap[Long, String]()
    val ooiIdAndUnit = SQL"""SELECT ooi.id, unit.name FROM ooi, unit WHERE ooi.unit_id = unit.id"""
      .as(long("ooi.id") ~ str("unit.name") *).toList
    for(entry <- ooiIdAndUnit) { newCache(entry._1) = entry._2 }
    cache = newCache
  }

  def getUnit(ooiId: Long) : Option[String] = cache.get(ooiId)
}
