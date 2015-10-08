package shared.matchers.test

import shared.matchers.{StringQueryMatcher, YearMatcher}
import org.specs2.runner._
import org.junit.runner._
/**
 * Created by nico on 08/10/15.
 *
 * Tests for the query parser
 */
class YearParserSpec extends QueryParserBase {

  // Define tests
  val queries = List(
    "not a year" -> Map(
      new YearMatcher() -> List()
    ),
    "1999" -> Map(
      new YearMatcher() -> List((0, 3))
    ),
    "a 1999 & 2015" -> Map(
      new YearMatcher() -> List((2, 5), (9, 12))
    ),
    "a1999" -> Map(
      new YearMatcher() -> List()
    ),
    "1999a" -> Map(
      new YearMatcher() -> List()
    )
  )

  performTests(queries)
}
