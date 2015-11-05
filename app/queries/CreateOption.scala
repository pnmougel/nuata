package queries

/**
 * Created by nico on 14/10/15.
 */

object CreateOption extends Enumeration {
  type CreateOption = Value
  val Always, Never, IfNameNotMatching, IfNotMatching = Value

  val createOptionsMap = Map(
      "never" -> CreateOption.Never,
      "always" -> CreateOption.Always,
      "ifnamenotmatching" -> CreateOption.IfNameNotMatching,
      "ifnotmatching" -> CreateOption.IfNotMatching
    )
  def fromString(valueStrOpt: Option[String]): Option[CreateOption] = {
    for(value <- valueStrOpt; createOption <- createOptionsMap.get(value.toLowerCase)) yield { createOption }
  }
}

case class CreateOption()
