package controllers

import com.codahale.jerkson.Json

import play.api._
import play.api.mvc._

import models._

object Transports extends Controller {
  
  def all = Action(parse.raw) { request =>
    val transports = Transport.findAll()
    val json = Json.generate(transports)
    
    generateResponseForRequest(request, json)
  }
  
  def one(id : Long) = Action(parse.raw) { request =>
    val transport = Transport.find(id)
    val json = Json.generate(transport)
    
    generateResponseForRequest(request, json)
  }
  
  def linesForTransportWithId(id : Long) = Action(parse.raw) { request =>
    val lines = Line.findAllFromTransport(id)
    val json = Json.generate(lines)
    
    generateResponseForRequest(request, json)
  }
  
  def generateResponseForRequest(request : Request[RawBuffer], json : String) = {
    if(request.queryString.contains("callback")) {
      Ok(request.queryString.get("callback").get.head + "(" + json + ")").as("text/javascript")
    } else {
      Ok(json).as("application/json")
    }
  }
  
}