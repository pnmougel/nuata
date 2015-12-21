package controllers

import com.github.tototoshi.play2.json4s.jackson.Json4s
import com.optimaize.langdetect.LanguageDetectorBuilder
import com.optimaize.langdetect.ngram.NgramExtractors
import com.optimaize.langdetect.profiles.LanguageProfileReader
import com.optimaize.langdetect.text.CommonTextObjectFactories
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.{Action, Controller}
import repositories.{DimensionRepository, OoiRepository, UnitRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by nico on 02/11/15.
 */
class LanguageDetector extends Controller with Json4s {
  def detect(text: String) = Action.async {
    val textObject = LanguageDetectorConfig.textObjectFactory.forText(text)
    val zz = LanguageDetectorConfig.languageDetector.detect(textObject)
    if(zz.isPresent) {
      Future.successful(Ok(zz.get().getLanguage))
    } else {
      Future.successful(Ok(""))
    }
  }
}

object LanguageDetectorConfig {
  val languageProfiles = new LanguageProfileReader().readAllBuiltIn()
  val languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
    .withProfiles(languageProfiles)
    .build()
  val textObjectFactory = CommonTextObjectFactories.forDetectingShortCleanText()
}
