package insee.crawl

import java.io.PrintWriter

import scalaj.http.Http
import java.io.File

/**
 * Created by nico on 10/10/15.
 */
object Utils {
  val baseStructurePath = "insee/data/structure/"

  private val baseUrl = "http://www.insee.fr"

  def getPage(url: String) = {
    val x = s"$baseUrl/$url"
    val res = Http(x).charset("iso-8859-1").asString
    //    println(res.body)
    res.body
  }

  def withWriter(file: File, f: PrintWriter => Unit) = {
    if(!file.exists) {
      file.getParentFile.mkdirs()
    }
    val bw = new PrintWriter(file)
    f(bw)
    bw.flush()
    bw.close()
  }
}
