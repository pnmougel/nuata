package models


/**
 * Created by nico on 13/10/15.
 */
case class Category(
              id: Option[String],
              ref: String,
              names: Map[String, List[String]],
              descriptions: Map[String, List[String]],
              create: Option[String])
  extends SearchableItem(
              "nuata" -> "category",
              id,
              names,
              descriptions,
              create, CreateOption.IfNameNotMatching) {

  def buildRefMapping(createQuery: CreateQuery): Unit = {
    createQuery.categoryRefMapping(ref.toLowerCase) = this
  }

  def resolveDependencies(createQuery: CreateQuery): Unit = { }
}
