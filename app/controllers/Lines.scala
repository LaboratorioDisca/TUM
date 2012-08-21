package controllers

import com.codahale.jerkson.Json

import play.api._
import play.api.mvc._

import models._

object Lines extends Controller {
	
  def all() = Action {
    val lines = Line.findAll()
    val json = Json.generate(lines)
    Ok(json).as("application/json").withHeaders(("Accept-Charset","utf-8"))
  }
  
  def one(id: Long) = Action {
    val line = Line.find(id)
    val json = Json.generate(line)
    Ok(json).as("application/json")
  }
  
}