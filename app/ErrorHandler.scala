import java.io.File

import play.api.Play
import play.api.http.HttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent._

class ErrorHandler extends HttpErrorHandler {
  val path = new File("app")
  val filesInProject = (for(file <- path.listFiles(); if(file.isDirectory)) yield {
    file.getName
  }).toSet

  def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    val statusError = statusCode match {
      case 404 => "Not found"
      case 500 => "Internal server error"
      case _ => "Unknown error"
    }
    Future.successful(
      Status(statusCode)(Json.obj("error" -> statusError, "message" -> message))
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable) = {
    System.err.println("A server error occured")
    exception.printStackTrace()
//    exception.printStackTrace()
    val stack = exception.getStackTrace.map( stackElement => {
      Json.obj(
        "file" -> stackElement.getFileName,
        "class" -> stackElement.getClassName,
        "line" -> stackElement.getLineNumber,
        "method" -> stackElement.getMethodName)
    })
    val stackInCode = exception.getStackTrace.filter(e => {
      filesInProject.exists( pack => e.getClassName.startsWith(pack))
    }) map( stackElement => {
      Json.obj(
        "file" -> stackElement.getFileName,
        "class" -> stackElement.getClassName,
        "line" -> stackElement.getLineNumber,
        "method" -> stackElement.getMethodName)
    })
    val res = Json.obj("message" -> exception.getMessage, "stackLocal" -> stackInCode, "stackFull" -> stack)
//    val res = Json.obj("message" -> exception.getMessage)
    Future.successful(
      InternalServerError(res)
    )
  }
}