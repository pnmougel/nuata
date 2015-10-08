package categories.test

import categories.{Category, CategoryQuery}
import languages.NameWithLanguage
import org.specs2.mutable.Specification
import org.specs2.specification.core.Fragment
import play.Application
import play.api.GlobalSettings
import play.api.db.evolutions.Evolutions
import play.api.test._
import shared.Query
import org.specs2.runner._
import org.junit.runner._
import play.api.db._

/**
 * Created by nico on 08/10/15.
 */
@RunWith(classOf[JUnitRunner])
class CategoriesModelSpec extends Specification {
  val dbConfig = Helpers.inMemoryDatabase(options = Map("MODE" -> "PostgreSQL"))
  val appWithMemoryDatabase = FakeApplication(additionalConfiguration = dbConfig)

  "run an application" in new WithApplication(appWithMemoryDatabase) {
    val categoryQuery = CategoryQuery(Some("Description 78"), List(NameWithLanguage("Category1", "fr")))
    Category.batchInsert(List(categoryQuery))

    for(c <- Category.findAll()) {
      println(c)
    }
  }
}
