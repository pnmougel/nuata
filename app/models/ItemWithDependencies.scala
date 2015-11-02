package models

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
/**
 * Created by nico on 16/10/15.
 */
trait ItemWithDependencies {
  def resolveDependencies(createQuery: CreateQuery): Unit

  /**
   * Returns the list of ids given a list of searchable items
   * @param items
   * @return
   */
  protected def getSearchIds(items: List[SearchableItem]) = {
    Future.sequence(
      items.map { item => item.searchItem().map { _.id } }
    )
  }
}
