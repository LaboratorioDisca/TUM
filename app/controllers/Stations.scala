package controllers

import com.codahale.jerkson.Json

import play.api._
import play.api.mvc._

import models._

object Stations extends Controller {

  def all = Action {
    val stations = Station.findAll()
 
    val json = Json.generate(stations)
    Ok(json).as("application/json")
  }
  
  def one(id : Long) = Action {
    val station = Station.find(id)
    val json = Json.generate(station)
    Ok(json).as("application/json")
  }
}