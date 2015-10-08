package shared.matchers

import shared.Query

/**
 * Created by nico on 08/10/15.
 *
 * Match a year formed by 4 digits
 */
class YearMatcher extends QueryMatcher {
  def updateMatch(query: Query, idx: Int, curCharToMatch: Char, isStartOfWord: Boolean, isEndOfQuery: Boolean): Unit = {
    if(isStartOfWord) {
      val querySize = query.trimLower.length
      var i = 0
      var isMatching = true
      while(isMatching && i != 4) {
        val curChar = query.trimLower.charAt(idx + i)
        isMatching = curChar >= '0' && curChar <= '9'
        i += 1
      }
      if(i == 4 && isMatching) {
        val isEndOfQuery = querySize == idx + i
        if(isEndOfQuery || (!isEndOfQuery && query.trimLower.charAt(idx + i) == ' ')) {
          matches :+= (idx, idx + 3)
        }
      }
    }
  }
}
