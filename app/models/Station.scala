
package models

import play.api.db._
import play.api.Play.current
import play.api.libs.json.Format

import com.vividsolutions.jts.geom.Point

import anorm._
import anorm.SqlParser._

case class Station(
    id: Long, 
    name: String, 
    isTerminal : Boolean, 
    isAccessible : Boolean, 
    bikeParkingType : Int, 
    lineId: Long,
    coordinate: Map[String, Double])

object Station {
 
  def findAll() : Seq[Station] = {
    DB.withConnection { implicit connection =>
      SQL(Station.query).as(Station.tuple *)
    }
  }
  
  def find(id : Long) : Station = {
    DB.withConnection { implicit connection =>
      SQL(Station.query+" WHERE id = {station_id}").on("station_id" -> id).as(Station.tuple.single)
    }
  }
  
  val query = "SELECT id, name, is_accessible, is_terminal, bike_parking, line_id, ST_AsText(coordinates) AS coordinates FROM stations"
  
  val tuple = {
    get[Long]("id") ~
    get[String]("name") ~ 
    get[Boolean]("is_terminal") ~
    get[Boolean]("is_accessible") ~
    get[Int]("bike_parking") ~
    get[Long]("line_id") ~
    get[String]("coordinates") map {
      case id~name~isTerminal~isAccessible~bikeParkingType~lineId~coordinate => 
        Station(id, name, isTerminal, isAccessible, bikeParkingType, lineId, parseCoordinateString(coordinate))
    }
  }
  
  def parseCoordinateString(coordinate : String) : Map[String, Double] = {
    val geom = new com.vividsolutions.jts.io.WKTReader().read(coordinate)
    
    geom match {
    	case point: Point => return Map("lon" -> point.getX(), "lat" -> point.getY())
    	case _ => throw new ClassCastException
    }
  }
 
/*  def create(station: Station): Unit = {
    DB.withConnection { implicit connection =>
      SQL("insert into stations(name) values ({name})").on(
        'name -> station.name
      ).executeUpdate()
    }
  }*/
 
}