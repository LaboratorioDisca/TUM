package controllers

import com.codahale.jerkson.Json

import play.api._
import play.api.mvc._

import models._

object Instants extends Controller {
	
  def all() = Action {
    val instants = Instant.findAll()
    val json = Json.generate(instants)
    Ok(json).as("application/json")
  }
  
  def recent() = Action {
    val instants = Instant.findAllRecent()
    val json = Json.generate(instants)
    Ok(json).as("application/json")
  }
  
  def aMinuteAgo() = Action {
    val instants = Instant.findAllInLastMinute()
    val json = Json.generate(instants)
    Ok(json).as("application/json")
  }
  
  def minutesAgo(minutes : Int) = Action {
    val instants = Instant.findAllInLastMinutes(minutes)
    val json = Json.generate(instants)
    Ok(json).as("application/json")
  }
  
  def reportingStatus() = Action {
    val instantCount = Instant.serviceStatus()
    val json = Json.generate(instantCount)
    Ok(json).as("application/json")
  }
}
