package controllers

import com.codahale.jerkson.Json

import play.api._
import play.api.mvc._

import models._

object Instants extends Controller {
	
  def all() = Action {
    val lines = Line.findAll()
    val json = Json.generate(lines)
    Ok(json).as("application/json")
  }
}