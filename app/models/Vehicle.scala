package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._

case class Vehicle(id : Long, identifier : Long, lineId : Long)

object Vehicle {
  
  def findAll() : Seq[Vehicle] = {
    DB.withConnection { implicit connection =>
      SQL(Vehicle.query).as(Vehicle.tuple *)
    }
  }
  
  def find(id : Long) : Vehicle = {
    DB.withConnection { implicit connection =>
      SQL(Vehicle.query+" WHERE id = {vehicle_id}").on("vehicle_id" -> id).as(Vehicle.tuple.single)
    }
  }
  
  def findByIdentifier(identifier : Long) : Vehicle = {
    DB.withConnection { implicit connection =>
      val vehicle : Seq[Vehicle] = SQL(Vehicle.query+" WHERE identifier = {identifier}").on("identifier" -> identifier).as(Vehicle.tuple *)
      if(vehicle.size > 0) {
    	return vehicle.head
      } else {
        return null
      }
    	    
    }
  }
  
  def findAllFromLine(id : Long) : Seq[Vehicle] = {
    DB.withConnection { implicit connection =>
      SQL(Vehicle.query+" WHERE line_id = {lineId}").on("lineId" -> id).as(Vehicle.tuple *)
    }
  }
  
  val query = "SELECT id, identifier, line_id FROM vehicles"
  val tuple = {
    get[Long]("id") ~
    get[Long]("identifier")~
    get[Long]("line_id") map {
      case id~identifier~lineId => 
        Vehicle(id, identifier, lineId)
    }
  }
}