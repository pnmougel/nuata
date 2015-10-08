package shared.matchers

import shared.Query

/**
 * Created by nico on 08/10/15.
 *
 * A query matcher for a string
 */
class StringQueryMatcher(val stringToMatch: String) extends QueryMatcher {
  private val stringToMatchLength = stringToMatch.size

  private var curIdx = 0
  private var hasStartedMatching = false
  private val firstChar = stringToMatch.charAt(0)

  def hasMatch = matches.nonEmpty

  def updateMatch(query: Query, idx: Int, curCharToMatch: Char, isStartOfWord: Boolean, isEndOfQuery: Boolean): Unit = {
    if(!hasStartedMatching) {
      hasStartedMatching = firstChar == curCharToMatch && isStartOfWord
      if(hasStartedMatching) curIdx = 1
    } else {
      val curChar = stringToMatch(curIdx)
      curIdx += 1
      val matchFound = (curChar == curCharToMatch) && curIdx == stringToMatchLength && (isEndOfQuery || (!isEndOfQuery && query.trimLower.charAt(idx + 1) == ' '))
      val startOfMatch = idx - stringToMatchLength + 1
      if(matchFound) matches :+= (startOfMatch, idx)
      val invalidMatch = curChar != curCharToMatch
      val isWordInQueryNotEnded = curIdx == stringToMatchLength && !isEndOfQuery && query.trimLower.charAt(idx + 1) != ' '
      if(matchFound || curChar != curCharToMatch || isWordInQueryNotEnded) {
        hasStartedMatching = false
        curIdx = 0
      }
    }
  }

  private def updateMatchOld(query: Query, idx: Int, curCharToMatch: Char, isStartOfWord: Boolean, isEndOfQuery: Boolean): Unit = {
    if(isStartOfWord) {
      val querySize = query.trimLower.length
      var i = 0
      var isMatching = firstChar == curCharToMatch
      while(isMatching && i != stringToMatchLength) {
        isMatching = stringToMatch.charAt(i) == query.trimLower.charAt(idx + i)
        i += 1
      }
      if(i == stringToMatchLength && isMatching) {
        val isEndOfQuery = querySize == idx + i
        if(isEndOfQuery || (!isEndOfQuery && query.trimLower.charAt(idx + i) == ' ')) {
          matches  :+ (idx, idx + querySize - 1)
        }
      }
    }
  }
}
