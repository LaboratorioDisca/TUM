package models

import play.api.db._
import play.api.Play.current
import play.api.libs.json.Format
import com.vividsolutions.jts.geom.Point
import anorm._
import anorm.SqlParser._
import java.util.Date

case class Instant(
    id : Long, 
    speed : Double, 
    isOld : Boolean, 
    hasHighestQuality : Boolean, 
    vehicleId : Long, 
    createdAt : Date, 
    coordinate : Map[String, Double])

object Instant {
  
  def findAll() : Seq[Instant] = {
    DB.withConnection { implicit connection =>
      SQL(Instant.query).as(Instant.tuple *)
    }
  }
  
  def find(id : Long) : Instant = {
    DB.withConnection { implicit connection =>
      SQL(Instant.query+" WHERE id = {instant_id}").on("instant_id" -> id).as(Instant.tuple.single)
    }
  }
  
  val query = "SELECT id, speed, is_old, has_highest_quality, vehicle_id, created_at, ST_AsText(coordinates) AS coordinates FROM instants"
  
  val tuple = {
    get[Long]("id") ~
    get[Double]("speed") ~ 
    get[Boolean]("is_old") ~
    get[Boolean]("has_highest_quality") ~
    get[Long]("vehicle_id") ~
    get[Date]("created_at") ~
    get[String]("coordinates") map {
      case id~speed~isOld~hasHighestQuality~vehicleId~createdAt~coordinate => 
        Instant(id, speed, isOld, hasHighestQuality, vehicleId, createdAt, parseCoordinateString(coordinate))
    }
  }
  
  def parseCoordinateString(coordinate : String) : Map[String, Double] = {
    val geom = new com.vividsolutions.jts.io.WKTReader().read(coordinate)
    
    geom match {
    	case point: Point => return Map("lon" -> point.getX(), "lat" -> point.getY())
    	case _ => throw new ClassCastException
    }
  }
  
}