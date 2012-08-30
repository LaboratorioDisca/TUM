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
  
  def recent() = Action(parse.raw) { request =>
    Logger.info("Loading most recent instants")
    val instants : Promise[Seq[Instant]] = Akka.future { Instant.findAllRecent() }
    
    generateResponseForRequest(request, instants)
  }
  
  def aMinuteAgo() = Action(parse.raw) { request =>
    Logger.info("Loading instants from a minute ago")
    val instants : Promise[Seq[Instant]] = Akka.future { Instant.findAllInLastMinute() } 
    
    generateResponseForRequest(request, instants)
  }
  
  def minutesAgo(minutes : Int) = Action(parse.raw) { request =>
    Logger.info("Loading instants from " + minutes.toString() +" ago")
    val instants : Promise[Seq[Instant]] = Akka.future { Instant.findAllInLastMinutes(minutes) }
    
    generateResponseForRequest(request, instants)
  }
  
  def reportingStatus() = Action(parse.raw) { request =>
    Logger.info("Fetching status")
    val instantCount : Promise[Int] = Akka.future { Instant.serviceStatus()  }

    generateResponseForRequest(request, instantCount)
  }
  
  // Helper special methods for JSONP and for concurrency improvements
  
  def generateResponseForRequest(request : Request[RawBuffer], promise : Promise[Any]) = {
    if(request.queryString.contains("callback")) {
      this.jsonPResponseForPromise(promise, request.queryString.get("callback").get.head)
    } else {
      this.responseForPromise(promise)
    }
  }
  
  def responseForPromise(promise : Promise[Any]) = Async {
    promise.map(response => Ok(Json.generate(response)).as("application/json"))
  }
  
  def jsonPResponseForPromise(promise : Promise[Any], callback : String) = Async {
    promise.map(response => Ok(callback + "(" + Json.generate(response) + ")").as("text/javascript"))
  }
}
