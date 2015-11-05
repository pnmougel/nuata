package models

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.{MatchQueryDefinition, QueryDefinition}

import scala.concurrent.Future

/**
 * Created by nico on 04/11/15.
 */
abstract class LocalizedNamedModel(
                                    _id: Option[String],
                                    _score: Option[Double],
                                    names: Map[String, List[String]],
                                    descriptions: Map[String, String])
  extends BaseModel(_id, _score) {

  def getIndexQuery : Map[String, Any]
  val defaultIndexQuery = Map("names" -> names, "descriptions" -> descriptions)

  def getSearchQuery: List[QueryDefinition]
  val defaultSearchQuery = (for((lang, localizedName) <- names) yield {
    filteredQuery filter termsFilter(s"names.${lang}.raw", localizedName:_*)
  }).toList

  val defaultMatchQuery = nestedQuery("names").query( bool { should {
    for((lang, localizedName) <- names) yield { com.sksamuel.elastic4s.ElasticDsl.matchQuery(lang, localizedName) }
  } } )
  def getMatchQuery: QueryDefinition
}
