package querybuilder.serializers

import org.json4s.{Extraction, ShortTypeHints, NoTypeHints, CustomSerializer}
import org.json4s.JsonAST._
import org.json4s.jackson.Serialization
import querybuilder._

/**
 * Created by nico on 20/10/15.
 */
class QuerySerializer extends CustomSerializer[Query](format => (
  { case _ => null },
  { case query: Query =>
    implicit val formats = Serialization.formats(NoTypeHints) + new Category() + new Dimension() + new FactUnit() + new OOI() + new FactSerializer()

    JObject(
      JField("categories", Extraction.decompose(query.categories))
        :: JField("dimensions", Extraction.decompose(query.dimensions))
        :: JField("units", Extraction.decompose(query.units))
        :: JField("oois", Extraction.decompose(query.oois))
        :: JField("facts", Extraction.decompose(query.facts))
        :: Nil
    )
  }))
