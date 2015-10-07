package services.timers

/**
 * Created by nico on 07/10/15.
 */
import scala.collection.mutable.HashMap

object Timer {
  private val deltas = HashMap[String, Double]()
  private val timers = HashMap[String, Double]()

  def clearTimers() = {
    deltas.clear()
    timers.clear()
  }

  def startTimer(timerName : String) = {
    deltas(timerName) = System.currentTimeMillis()
  }

  def stopTimer(timerName : String) = {
    timers(timerName) = timers.getOrElse(timerName, 0D) + System.currentTimeMillis() - deltas(timerName)
  }

  def printTimers() = {
    println("# \n# Timers")

    var maxLength = 0
    timers.keys.foreach { k => maxLength = if(k.size > maxLength) k.size else maxLength }

    timers.toList.sortBy({_._2}).foreach { a =>
      println("# " + a._1 + ":" + " " * (1 + maxLength - a._1.size) + a._2 / 1000)
    }
  }

}
