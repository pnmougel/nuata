package querybuilder

import org.json4s.JsonAST.{JString, JArray}

/**
 * Created by nico on 19/11/15.
 */
trait ItemWithDependencies {

  def getIds(items: Seq[JsonSerializable]) = {
    val missingItems = items.filter(i => !i._id.isDefined)
    if(!missingItems.isEmpty) {
      println(s"Dependency missing for ${this.getClass.getSimpleName}")
      println(missingItems.map(_.nameToString).mkString(" - ", "\n - ", ""))
    }
    JArray(items.filter(item => item._id.isDefined).map(item => JString(item._id.get)).toList)
  }
}
