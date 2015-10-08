package shared.matchers.test

import org.specs2.mutable._
import shared.Query
import shared.matchers.{DateMatcher, StringQueryMatcher, YearMatcher}
import org.specs2.runner._
import org.junit.runner._
/**
 * Created by nico on 08/10/15.
 *
 * Tests for the query parser
 */
class StringQueryParserSpec extends QueryParserBase {

  // Define tests
  val queries = List(
    "as ab ab" -> Map(
      new StringQueryMatcher("as") -> List((0, 1)),
      new StringQueryMatcher("ab") -> List((3, 4), (6, 7)),
      new StringQueryMatcher("xy") -> List()
    ),
    "xyz" -> Map(
      new StringQueryMatcher("xyz") -> List((0, 2)),
        new StringQueryMatcher(" ") -> List()
    ),
    "abc" -> Map(
      new StringQueryMatcher("ab") -> List(),
        new StringQueryMatcher("bc") -> List()
    ),
    "xyz bc" -> Map(
      new StringQueryMatcher("bc") -> List((4, 5))
    )
  )

  performTests(queries)
}
