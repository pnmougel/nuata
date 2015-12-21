package xls

import java.io.{File, FileInputStream}

import com.github.tototoshi.play2.json4s.jackson.Json4s
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.json4s.DefaultFormats
import play.api.mvc.{Action, Controller}
import scala.collection.JavaConversions._

/**
 * Created by nico on 02/12/15.
 */
case class XlsCell(value: String, rowIdx: Int, colIdx: Int, `type`: String)
case class XlsRow(cells: Vector[XlsCell], rowIdx: Int)
case class XlsSheet(name: String, rows: Vector[XlsRow])

class XlsAnalyzer extends Controller with Json4s {
  implicit val formats = DefaultFormats
  import org.json4s._
  import org.json4s.jackson.Serialization.{read, write}


  def isDataRow(row: XlsRow) = {
    row.cells.length >= 2 && !row.cells.exists(cell => cell.`type` != "number" && cell.colIdx != 0)
  }

  def findUnit(sheet: XlsSheet) = {
    for(row <- sheet.rows) {
      for((cell, idx) <- row.cells.zipWithIndex) {
        if(cell.value.isEmpty) {

        }
      }
    }
  }

  def analyze = Action(parse.temporaryFile) { request =>
    val workbook = WorkbookFactory.create(request.body.file)
    val nbSheets = workbook.getNumberOfSheets
    val sheets = for(sheetIdx <- 0 until nbSheets) yield {
      val sheet = workbook.getSheetAt(sheetIdx)
      val rowValues = for((row, rowIdx) <- sheet.iterator().zipWithIndex) yield {
        val cellValues = for((cell, colIdx) <- row.cellIterator().zipWithIndex) yield {
          cell.getCellType() match {
            case Cell.CELL_TYPE_NUMERIC => XlsCell(cell.getNumericCellValue.toString, rowIdx, colIdx, "number")
            case Cell.CELL_TYPE_STRING => XlsCell(cell.getStringCellValue, rowIdx, colIdx, "string")
            case _ => XlsCell("", rowIdx, colIdx, "unknown")
          }
        }
        XlsRow(cellValues.toVector, rowIdx)
      }
      XlsSheet(sheet.getSheetName, rowValues.toVector)
    }

    Ok(Extraction.decompose(sheets))
  }

}
