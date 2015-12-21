package models

import org.json4s.JsonAST.JValue
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by nico on 26/11/15.
 */
trait JsonSerializable {
  def toJson: Future[JValue]

  def toJsonSeq(items: Future[Seq[JsonSerializable]]) = items.flatMap { items =>
    Future.sequence(items.map(item => item.toJson))
  }
}
