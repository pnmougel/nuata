package repositories

import repositories.NameOperations.NameOperations

/**
 * Created by nico on 08/12/15.
 */
case class SearchOptions(name: String, nameOperation: NameOperations, start: Int = 0, limit: Int = 10, filters: Map[String, List[String]] = Map()) {

}
