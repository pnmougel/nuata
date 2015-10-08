package shared

import shared.matchers.{YearMatcher, StringQueryMatcher, QueryMatcher}

import scala.collection._
/**
 * Created by nico on 08/10/15.
 *
 * A user query
 */
class Query(base: String) {
  val trimLower = base.toLowerCase.trim
  val matchers = mutable.HashSet[QueryMatcher]()
  val queryLength = trimLower.size

  def addMatcher(matcher: QueryMatcher) = matchers.add(matcher)
  def removeMatcher(matcher: QueryMatcher) = matchers.remove(matcher)

  // time related words
  val allWordsToMatch = List("between", "and", "since", "before", "in", "from", "to")
  val wordsMatchers = mutable.HashMap(
    (for(w <- allWordsToMatch) yield {
      val newMatcher = new StringQueryMatcher(w)
      addMatcher(newMatcher)
      (w, newMatcher)
    }): _*)
  val yearParser = new YearMatcher()
  addMatcher(yearParser)

  /**
   * Parse the query
   */
  def parse(): Unit = {
    // Clear the previous results
    for(m <- matchers) { m.matches = Vector() }

    // Iterate over the characters of the query
    var i = 0
    var isStartOfQuery = true
    while(i != queryLength) {
      val isStartOfWord = isStartOfQuery || (!isStartOfQuery && trimLower.charAt(i - 1) == ' ')
      val isEndOfQuery = i == queryLength - 1
      val c = trimLower.charAt(i)
      for(m <- matchers) m.updateMatch(this, i, c, isStartOfWord, isEndOfQuery)
      i += 1
      isStartOfQuery = false
    }
  }

  parse()
  var isYearInterval = false
  if(wordsMatchers("between").hasMatch) {
    if(yearParser.matches.size == 2) {
      isYearInterval = true
    }
  }
}
