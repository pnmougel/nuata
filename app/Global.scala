import play.api.GlobalSettings
import play.api._
import services.caches.NameCache

/**
 * Created by nico on 07/10/15.
 */
object Global extends GlobalSettings {
  override def onStart(app: Application) = {
//    NameCache.build()
    Logger.info("Application has started 2")
  }
}
