package units

import anorm._
import play.api.Play.current
import play.api.db._

import scala.collection.mutable

/**
 * Created by nico on 01/10/15.
 *
 */
case class Unit(id: Long, name: String) {}

object Unit {
  val table = "Unit"

  // Unit cache
  val unitCache = mutable.HashMap[String, Long]()

  def getOrCreate(name: String) : Long = {
    unitCache.getOrElseUpdate(name, {
      DB.withConnection { implicit c =>
        val unitId = SQL"""SELECT id FROM #$table WHERE name = $name""".as(SqlParser.long("id").singleOpt)
        unitId.getOrElse {
          println("Trying to build unit " + name)
          SQL"""INSERT INTO #$table (name) VALUES ($name)""".executeInsert().asInstanceOf[Option[Long]].getOrElse(0)
        }
      }
    })
  }
}