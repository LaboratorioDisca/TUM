package controllers

import com.codahale.jerkson.Json

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Promise
import play.api.Play.current

import models._

object Vehicles extends Controller {

  def all = Action {
    val vehicles = Vehicle.findAll()
    val json = Json.generate(vehicles)
    Ok(json).as("application/json")
  }
  
  def one(id : Long) = Action {
    val vehicle = Vehicle.find(id)
    val json = Json.generate(vehicle)
    Ok(json).as("application/json")
  }
  
  def allFromLineWithId(id : Long) = Action {
    val vehicles : Promise[Seq[Vehicle]] = Akka.future { Vehicle.findAllFromLine(id) }
    
    Async {
      vehicles.map(sequence => Ok(Json.generate(sequence)).as("application/json"))
    }
  }
}