package models

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.{BoolQueryDefinition, MatchQueryDefinition, QueryDefinition}
import org.json4s.JsonAST.JString

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

  def getSearchQuery: BoolQueryDefinition
  val defaultSearchQuery = should { (for((lang, localizedName) <- names) yield {
    filteredQuery filter termsFilter(s"names.${lang}.raw", localizedName:_*)
  }).toList }

  val defaultMatchQuery = nestedQuery("names").query( bool { should {
    for((lang, localizedName) <- names) yield { com.sksamuel.elastic4s.ElasticDsl.matchQuery(lang, localizedName) }
  } } )
  def getMatchQuery: QueryDefinition

  def isPerfectMatch(res: LocalizedNamedModel): Boolean
}
