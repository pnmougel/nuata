package models

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.{BoolQueryDefinition, MatchQueryDefinition, QueryDefinition}
import org.json4s.JsonAST.JString

import scala.concurrent.Future

/**
 * Created by nico on 04/11/15.
 */
abstract class LocalizedNamedModel(_id: Option[String],
                                   _score: Option[Double],
                                   name: Map[String, String],
                                   otherNames: Map[String, List[String]],
                                   description: Map[String, String],
                                   meta: Map[String, _])
  extends BaseModel(_id, _score) with JsonSerializable {

  def getIndexQuery : Map[String, Any]
  val defaultIndexQuery = Map("name" -> name, "otherNames" -> otherNames, "description" -> description, "meta" -> meta)

  def getSearchQuery: BoolQueryDefinition
  val defaultSearchQuery = should { (for((lang, localizedName) <- otherNames) yield {
    filteredQuery filter termsFilter(s"names.${lang}.raw", localizedName:_*)
  }).toList }

  val defaultMatchQuery = nestedQuery("names").query( bool { should {
    for((lang, localizedName) <- otherNames) yield { com.sksamuel.elastic4s.ElasticDsl.matchQuery(lang, localizedName) }
  } } )
  def getMatchQuery: QueryDefinition
}
