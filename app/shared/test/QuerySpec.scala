package shared.test

import org.specs2.mutable.Specification
import org.specs2.specification.core.Fragment
import shared.Query
import org.specs2.runner._
import org.junit.runner._

/**
 * Created by nico on 08/10/15.
 */
class QuerySpec extends Specification {
  val queries = List(
    ("between 1950 and 2015",
      List(("Should be a year interval", (q: Query) => q.isYearInterval must beTrue))
      ),
    ("between 999 and 2015", List(
      ("Should not be a year interval since 999 is not a year", (q: Query) => q.isYearInterval must beFalse))
      ),
    ("betweens 1950 and 2015", List(
      ("Should not be a year interval since between is not matched", (q: Query) => q.isYearInterval must beFalse))
      )
  )

  for ((queryStr, tests) <- queries) {
    val q = new Query(queryStr)
    s"The query '${queryStr}'" >> {
      Fragment.foreach(tests.toList) { case (message, f) =>
        message >> { f(q) }
      }
    }
  }
}
