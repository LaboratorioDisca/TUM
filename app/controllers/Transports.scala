package controllers

import com.codahale.jerkson.Json

import play.api._
import play.api.mvc._

import models._

object Transports extends Controller {
  
  def all = Action {
    val transports = Transport.findAll()
    val json = Json.generate(transports)
    Ok(json).as("application/json")
  }
  
  def one(id : Long) = Action {
    val transport = Transport.find(id)
    val json = Json.generate(transport)
    Ok(json).as("application/json")
  }
  
  def linesForTransportWithId(id : Long) = Action {
    val lines = Line.findAllFromTransport(id)
    val json = Json.generate(lines)
    Ok(json).as("application/json")
  }
  
}