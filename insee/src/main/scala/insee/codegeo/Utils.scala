package insee.codegeo

import scala.io.Source
import java.io.File

/**
 * Created by nico on 10/10/15.
 */
object Utils {
  val basePath = "insee/data/sources/code_geo/"
  def parseFile(name: String) : List[Array[String]] = {

    (for((line, i) <- Source.fromFile(basePath + name + ".txt", "ISO-8859-15").getLines().toList.zipWithIndex; if i != 0) yield {
      line.split("\t")
    })
  }
}
