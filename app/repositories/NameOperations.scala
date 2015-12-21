package repositories

/**
 * Created by nico on 08/12/15.
 */
object NameOperations extends Enumeration {
  type NameOperations = Value
  val StartsWith, Match, Exact = Value
}
