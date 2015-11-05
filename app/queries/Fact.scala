package queries

import com.sksamuel.elastic4s.ElasticDsl.{index, _}
import elasticsearch.ElasticSearch
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by nico on 14/10/15.
 */
case class Fact(value: Option[Double], valueInt: Option[Long], at: Option[String], dimensions: List[String], ooi: String) extends ItemWithDependencies {

  var resolvedDimensions = List[DimensionQuery]()
  var resolvedOois = List[OOI]()

  def resolveReferences(createQuery: CreateQuery): Unit = {
    resolvedDimensions = for(dimensionRef <- dimensions; dimension <- createQuery.dimensionRefMapping.get(dimensionRef.toLowerCase)) yield dimension

//    resolvedOois = List(createQuery.ooiRefMapping.get(ooi.toLowerCase).get)
  }

  lazy val dimensionIds = resolveIds(resolvedDimensions)
  lazy val ooiIds = resolveIds(resolvedOois)

  def searchItem(): Future[SearchResult] = {
    (for(dimensionIds <- dimensionIds; ooiIds <- ooiIds) yield {
      var values = Map[String, Any](
          "dimensionIds" -> dimensionIds,
          "ooiId" -> ooiIds)
      for(valueL <- valueInt) { values += ("valueInt" -> valueL) }
      for(valueD <- value) { values += ("value" -> valueD) }
      val query = index into "nuata" / "fact" fields values
      ElasticSearch.client.execute { query }.map { res =>
        val source = Json.parse(query._fieldsAsXContent.string)
        SearchResult(Some(res.getId), Some(source), isCreated = true, nbHits = 0, Seq(), idGivenButMissing = false)
      }
    }).flatMap(x => x)
  }
}
