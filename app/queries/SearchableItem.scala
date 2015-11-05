package queries

/**
 * Created by nico on 14/10/15.
 *
 */

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import elasticsearch.ElasticSearch
import models.BaseModel
import play.api.libs.json.Json
import shared.Languages

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class SearchableItem[T](
          location: (String, String),
          id: Option[String],
          names: Map[String, List[String]],
          descriptions: Map[String, List[String]],
          create: Option[String],
          defaultCreateOption: CreateOption.Value) {

  def getLocation = location
  def getId = id

  var createOption: CreateOption.Value = defaultCreateOption
  def initCreateOption(createQuery: CreateQuery) = {
    val curDefaultCreateOption = createQuery.createOption.getOrElse(defaultCreateOption)
    createOption = CreateOption.fromString(create).getOrElse(curDefaultCreateOption)
  }

  def indexQuery : Future[Map[String, Any]]

  val defaultInsertQuery = Map("names" -> names, "descriptions" -> descriptions)
  /**
   * Override this method to change the content of the inserted document
   * @return
   */

  val defaultExactMatchQuery = (for((lang, localizedName) <- names) yield {
    filteredQuery filter termsFilter(s"names.${lang}.raw", localizedName:_*)
  }).toList

  def exactMatchQuery : Future[List[QueryDefinition]]

  val defaultMatchQuery = nestedQuery("names").query( bool { should {
    for((lang, localizedName) <- names) yield { com.sksamuel.elastic4s.ElasticDsl.matchQuery(lang, localizedName) }
  } } )

  def matchQuery: Future[QueryDefinition] = Future(defaultMatchQuery)

  var defaultMatchQueries = List[MatchQueryDefinition]()
}
