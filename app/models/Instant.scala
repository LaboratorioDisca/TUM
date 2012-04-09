package models

import play.api.db._
import play.api.Play.current
import play.api.libs.json.Format
import com.vividsolutions.jts.geom.Point
import anorm._
import anorm.SqlParser._
import java.util.Date
import java.text.DateFormat
import java.util.Calendar
import play.api.Logger
import scala.collection.mutable.HashMap
import java.util.TimeZone
import java.util.GregorianCalendar

case class Instant(
    id : Long, 
    speed : Double, 
    isOld : Boolean, 
    hasHighestQuality : Boolean, 
    vehicleId : Long, 
    createdAt : Date, 
    coordinate : Map[String, Double])

object Instant {
  
  val timeZone = TimeZone.getTimeZone("Mexico_City")
  val query = "SELECT id, speed, is_old, has_highest_quality, vehicle_id, created_at, ST_AsText(coordinates) AS coordinates FROM instants"

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
  
  def findAllLastMinute() : Seq[Instant] = {
    DB.withConnection { implicit connection =>
      SQL(Instant.query+ "WHERE created_at <= {recent}").on("recent" -> this.getTimeBeforeGivenMinutes(1)).as(Instant.tuple *)
    }
  }
  
  
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
  
  def insertNew(vehicleData : HashMap[String, String]) {
    if(vehicleData("vehicleId").isEmpty() || vehicleData("age").isEmpty() || vehicleData("quality").isEmpty())
      return
    
	var vehicle = Vehicle.findByIdentifier(vehicleData("vehicleId").toLong)
	if(vehicle == null)
	  return

	val latitude = vehicleData("latitude") 
	val longitude = vehicleData("longitude") 	
	val date = getDateFromSeconds(vehicleData("seconds").toInt)
	val dateFormat = DateFormat.getDateTimeInstance()
	Logger.info("Attempting to save instant with date: " + dateFormat.format(date) + " with lon/lat: " + longitude + ","+ latitude)
  }
  
  def parseCoordinateString(coordinate : String) : Map[String, Double] = {
    val geom = new com.vividsolutions.jts.io.WKTReader().read(coordinate)
    
    geom match {
    	case point: Point => return Map("lon" -> point.getX(), "lat" -> point.getY())
    	case _ => throw new ClassCastException
    }
  }
  
  def getTimeBeforeGivenMinutes(minutes : Int) : Date = {
    val current : Calendar = Calendar.getInstance()
    current.set(Calendar.MINUTE, current.get(Calendar.MINUTE)-minutes)
    return current.getTime()
  }
  
  def getDateFromSeconds(seconds : Int) : Date = {
    Logger.info("Time in seconds: " + seconds)
    
    var hours = scala.math.floor(seconds/3600);
   
	val divMinutes = seconds % 3600;
	var minutes = scala.math.floor(divMinutes / 60);
 
	var divSeconds = divMinutes % 60;
    
	var calendar : Calendar = new GregorianCalendar(timeZone)
	Logger.info("Time current: " + calendar.getTimeInMillis()/1000)
	calendar.setTimeInMillis(seconds*1000)
	calendar.set(Calendar.HOUR, hours.toInt)
	calendar.set(Calendar.MINUTE, minutes.toInt)
	calendar.set(Calendar.SECOND, scala.math.ceil(divSeconds).toInt)
	
	return calendar.getTime()
  }
  
}