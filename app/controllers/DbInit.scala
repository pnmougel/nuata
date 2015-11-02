package controllers

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.mappings.{FieldType, StringFieldDefinition, MappingDefinition}
import elasticsearch.ElasticSearch
import org.elasticsearch.common.joda.time.DateTimeFieldType
import play.api.mvc.{Action, Controller}
import com.sksamuel.elastic4s.mappings.FieldType._

/**
 * Created by nico on 14/10/15.
 */
class DbInit extends Controller {
  def init = Action {
    case object DateTimeType extends FieldType("date_time")

    ElasticSearch.client.execute { deleteIndex("_all") }.await
    Thread.sleep(1000)

    val fields = List("names", "descriptions")
    val languages = Map("en" -> EnglishLanguageAnalyzer, "fr" -> FrenchLanguageAnalyzer)

    val baseQuery = for(field <- fields) yield {
      val langFields = for((lang, langAnalyzer) <- languages.toList) yield {
        lang typed StringType analyzer langAnalyzer fields("raw" typed StringType index "not_analyzed")
      }
      //
//       field inner (langFields : _*)
      field nested (langFields : _*)
    }

    val categoryIds = "categoryIds" typed StringType index "not_analyzed"
    val unitIds = "unitIds" typed StringType index "not_analyzed"

    val increaseQueueSizeSetting = Map(
      "threadpool.search.type" -> "fixed",
      "threadpool.search.size" -> "7",
      "threadpool.search.size" -> "4000",

      "threadpool.index.type" -> "fixed",
      "threadpool.index.size" -> "7",
      "threadpool.index.size" -> "4000"
    )


    ElasticSearch.client.execute(cluster.persistentSettings(increaseQueueSizeSetting)).await
    ElasticSearch.client.execute(cluster.transientSettings(increaseQueueSizeSetting)).await

    ElasticSearch.client.execute {
      create index "nuata" mappings(
        "category" as (baseQuery),
        "dimension" as (categoryIds :: baseQuery),
        "unit" as (baseQuery),
        "ooi" as (unitIds :: baseQuery),
        "fact" as (
          "value" typed DoubleType,
          "valueInt" typed LongType,
          "at" typed DateType,
          "dimensionIds" typed StringType index "not_analyzed",
          "ooiId" typed StringType index "not_analyzed"
        )
      )
    }.await
    Ok("Database rebooted")
  }
}
