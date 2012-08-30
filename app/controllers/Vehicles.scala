package controllers

import com.codahale.jerkson.Json

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Promise
import play.api.Play.current

import models._

object Vehicles extends Controller {

  def all = Action(parse.raw) { request =>
    val vehicles = Vehicle.findAll()
    val json = Json.generate(vehicles)
    
    generateResponseForRequest(request, json)
  }
  
  def one(id : Long) = Action(parse.raw) { request =>
    val vehicle = Vehicle.find(id)
    val json = Json.generate(vehicle)
    
    generateResponseForRequest(request, json)
  }
  
  def allFromLineWithId(id : Long) = Action(parse.raw) { request =>
    val vehicles : Promise[Seq[Vehicle]] = Akka.future { Vehicle.findAllFromLine(id) }
    
    generateResponseForRequest(request, vehicles)
  }

  def generateResponseForRequest(request : Request[RawBuffer], json : String) = {
    if(request.queryString.contains("callback")) {
      Ok(request.queryString.get("callback").get.head + "(" + json + ")").as("text/javascript")
    } else {
      Ok(json).as("application/json")
    }
  }
  
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