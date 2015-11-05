import play.api.GlobalSettings
import play.api._
import play.api.mvc.RequestHeader
import play.api.libs.json.Json._
import scala.concurrent.Future
import play.api.mvc.Results._
import play.api.mvc._

/**
 * Created by nico on 07/10/15.
 */
object Global extends GlobalSettings {
  override def onStart(app: Application) = {
//    NameCache.build()
    Logger.info("Application has started 2")
  }

  override def onHandlerNotFound(request : RequestHeader) : Future[Result] = {
    val errorJson = toJson(Seq(toJson("error"),
      toJson(Map(
        "type" -> toJson("handlerNotFound"),
        "path" -> toJson(request.path),
        "method" -> toJson(request.method)
      ))
    ))
    Future.successful(Ok(errorJson))
  }
}
