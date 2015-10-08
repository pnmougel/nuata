package shared.matchers.test

import shared.Query
import shared.matchers._
import org.specs2.mutable._
import org.specs2.specification.core.Fragment
import org.specs2.runner._
import org.junit.runner._
/**
 * Created by nico on 08/10/15.
 */
trait QueryParserBase extends Specification {
  // Perform the tests
  def performTests(queries : List[(String, scala.collection.immutable.Map[_ <: QueryMatcher, List[(Int, Int)]])]): Unit = {
    for((str, matches) <- queries) {
      val q = new Query(str)
      s"When the query is '$str'" >> {
        Fragment.foreach(matches.toList) { case (matcher, matchValues) =>
          q.addMatcher(matcher)
          q.parse
          val matcherName = matcher match {
            case x : StringQueryMatcher => s"'${x.stringToMatch}'"
            case x : YearMatcher => "YearMatcher"
          }
          s"and the matcher is ${matcherName}" >> {
            s"the matches should be ${matchValues}" >> {
              matcher.matches mustEqual matchValues
            }
          }
        }
      }
    }
  }
}
