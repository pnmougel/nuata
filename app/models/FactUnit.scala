package models


/**
 * Created by nico on 14/10/15.
 */
case class FactUnit(
    id: Option[String],
    ref:  String,
    names: Map[String, List[String]],
    descriptions: Map[String, List[String]],
    create: Option[String])
  extends SearchableItem(
    "nuata" -> "unit",
    id,
    names,
    descriptions,
    create, CreateOption.IfNameNotMatching) {

  def buildRefMapping(createQuery: CreateQuery): Unit = {
    createQuery.factUnitRefMapping(ref.toLowerCase) = this
  }

  def resolveDependencies(createQuery: CreateQuery): Unit = { }

}
