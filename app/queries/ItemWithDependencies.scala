package queries

import controllers.QueryProcessor
import models.{CategoryModel, DimensionModel}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
/**
 * Created by nico on 16/10/15.
 */
trait ItemWithDependencies {
  def resolveReferences(createQuery: CreateQuery): Unit

  /**
   * Returns the list of ids given a list of searchable items
   * @param items
   * @return
   */
  /*
  protected def getSearchIds(items: List[SearchableItem[_]]) = {
    Future.sequence(
      items.map { item => item.searchItem().map { _.id } }
    )
  }
  */

  var unresolvedDependencies = List[SearchableItem[_]]()
  var hasMissingRef = false

  protected def resolveIds(queries: List[SearchableItem[_]]): Future[List[String]] = {
    Future.sequence(queries.map( resolvedItem => {
      QueryProcessor.searchItem(resolvedItem).map( searchResult => {
        if(!searchResult.id.isDefined) {
          unresolvedDependencies = resolvedItem :: unresolvedDependencies
        }
        searchResult.id
      })
    })).map( optIds => optIds.filter(_.isDefined).map(_.get) )

    /*
    Future.sequence(queries.map( resolvedItem => {
      resolvedItem.resolve().map( itemOpt => {
        itemOpt match {
          case dimensionOpt : Option[DimensionModel] => {
            if(dimensionOpt.isDefined) {
              dimensionOpt.get._id
            } else {
              unresolvedDependencies = resolvedItem :: unresolvedDependencies
              None
            }
          }
          case categoryOpt : Option[CategoryModel] => {
            if(categoryOpt.isDefined) {
              categoryOpt.get._id
            } else {
              unresolvedDependencies = resolvedItem :: unresolvedDependencies
              None
            }
          }
          case _ => {
            None
          }
        }
      })
    })).map( ids => {
      ids.filter(idOpt => idOpt.isDefined).map(idOpt => idOpt.get)
    })
    */
  }
}
