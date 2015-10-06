package facts

import java.util.Date

import anorm.SqlParser._
import anorm._
import dimensions.Dimension
import languages.{Language, NameWithLanguage}
import play.api.Play.current
import play.api.db._

/**
 * Created by nico on 01/10/15.
 *
 * Model for the categories
 */
case class FactValue(id: Long, value: Either[Long, Double]) {}

case class FactIntWithDimensions(id: Long, value: Int, dimensions: List[Dimension]) {}

object Fact {
  val simple : RowParser[FactValue] = {
    get[Long]("id") ~ get[Option[Long]]("valueint") ~ get[Option[Double]]("valueFloat") map {
      case id ~ Some(value: Long) ~ None => FactValue(id, Left(value))
      case id ~ None ~ Some(value: Double) => FactValue(id, Right(value))
    }
  }

  def find(ooi: Long, dimensions: List[Long]) : List[FactValue] = {
    DB.withConnection { implicit c =>
      SQL"""
        SELECT id, valueInt, valueFloat FROM fact
        WHERE id IN
          (SELECT fact_id FROM fact_dimension WHERE dimension_id IN ($dimensions))
          AND ooi_id = $ooi
      """.as(simple *)
    }
  }

  def findFacts(oois: Seq[(Long, String, String)], dimensions: Seq[Long]): List[(FactValue, (Long, String, String), Seq[Long])] = {
    var matchingFacts = List[(FactValue, (Long, String, String), Seq[Long])]()
    val dimensionsSet = dimensions.toSet
    DB.withConnection { implicit c =>
      for (ooi <- oois) {
        val facts = SQL"""
          SELECT id, valueInt, valueFloat FROM fact
          WHERE ooi_id = ${ooi._1}
        """.as(simple *)

        for(fact <- facts) {
          val dimensionsForFact = SQL"""
            SELECT dimension_id FROM fact_dimension
            WHERE fact_id = ${fact.id}
            """.as(SqlParser.long("dimension_id").*).toSet
          val matchingDimensions = dimensionsForFact.intersect(dimensionsSet)
          if(matchingDimensions.nonEmpty) {
            matchingFacts = (fact, ooi, matchingDimensions.toList) :: matchingFacts
          }
        }
      }
    }
    matchingFacts
  }

  def insert(value: Either[Long, Double], ooi: Long, dimensions: List[Long]) : Option[Long] = {
    val now = new Date()
    DB.withConnection { implicit c =>
      // Insert the fact
      val factId: Option[Long] = value match {
        case Right(valueFloat) => {
          SQL"""INSERT INTO fact (valuefloat, ooi_id, created_at) VALUES ($valueFloat, $ooi, $now)""".executeInsert()
        }
        case Left(valueInt) => {
          SQL"""INSERT INTO fact (valueint, ooi_id, created_at) VALUES ($valueInt, $ooi, $now)""".executeInsert()
        }
      }

      // Add the relation to the related dimensions
      for(dimensionId <- dimensions) {
        SQL"""
          INSERT INTO fact_dimension
          (fact_id, dimension_id)
          VALUES ($factId, $dimensionId)
        """.executeInsert()
      }
      factId
    }
  }
}