package controllers

import com.codahale.jerkson.Json
import play.api._
import play.api.mvc._
import models._
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Promise
import play.api.Play.current

object Instants extends Controller {
	
  def all() = Action {
    val instants = Instant.findAll()
    val json = Json.generate(instants)
    Ok(json).as("application/json")
  }
  
  def recent() = Action {
    Logger.info("Loading most recent instants")

    val instants : Promise[Seq[Instant]] = Akka.future { Instant.findAllRecent() }
    
    this.responseForPromise(instants)
  }
  
  def aMinuteAgo() = Action {
    Logger.info("Loading instants from a minute ago")
    
    val instants : Promise[Seq[Instant]] = Akka.future { Instant.findAllInLastMinute() } 
    
    this.responseForPromise(instants)
  }
  
  def minutesAgo(minutes : Int) = Action {
    Logger.info("Loading instants from " + minutes.toString() +" ago")
    
    val instants : Promise[Seq[Instant]] = Akka.future { Instant.findAllInLastMinutes(minutes) }
    
    this.responseForPromise(instants)
  }
  
  def reportingStatus() = Action {
    Logger.info("Fetching status")
    
    val instantCount : Promise[Int] = Akka.future { Instant.serviceStatus()  }
    
    this.responseForPromise(instantCount)
  }
  
  def responseForPromise(promise : Promise[Any]) = Async {
      promise.map(status => Ok(Json.generate(status)).as("application/json"))
  }
}
