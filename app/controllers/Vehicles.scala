package controllers

import com.codahale.jerkson.Json

import play.api._
import play.api.mvc._

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
    val vehicles = Vehicle.findAllFromLine(id)
    val json = Json.generate(vehicles)
    Ok(json).as("application/json")
  }
}