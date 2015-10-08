package services

import play.api.libs.json.Json

/**
 * Created by nico on 07/10/15.
 */


object Lang {
  type Lang = String

  def en(name: String) = (name, "en")
  def fr(name: String) = (name, "fr")
}
