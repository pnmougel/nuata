package shared.matchers

import shared.Query

/**
 * Created by nico on 08/10/15.
 *
 * Abstract query matcher
 */
abstract class QueryMatcher {
  var matches = Vector[(Int, Int)]()

  def updateMatch(query: Query, idx: Int, curCharToMatch: Char, isStartOfWord: Boolean, isEndOfQuery: Boolean): Unit
}
