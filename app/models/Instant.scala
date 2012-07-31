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
import anorm.NotAssigned


case class Instant(
    id : Pk[Long], 
    speed : Double, 
    isOld : Boolean, 
    hasHighestQuality : Boolean, 
    vehicleId : Long, 
    createdAt : Date, 
    coordinate : Map[String, Double])

object Instant {
  
  val timeZone = TimeZone.getTimeZone("Mexico_City")
  val query = "SELECT id, speed, is_old, has_highest_quality, vehicle_id, created_at, ST_AsText(coordinates) AS coordinates FROM instants"
  val queryUniques = "SELECT DISTINCT ON (vehicle_id) id, speed, is_old, has_highest_quality, vehicle_id, created_at, ST_AsText(coordinates) AS coordinates FROM instants";

  val tuple = {
    get[Pk[Long]]("id") ~
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
  
  def findAllRecent() : Seq[Instant] = {
    DB.withConnection { implicit connection =>
      SQL(Instant.queryUniques + " WHERE is_old = 'f' ORDER BY vehicle_id, created_at DESC").as(Instant.tuple *)
    }
  }

  def findAllInLastMinute() : Seq[Instant] = {
    this.findAllInLastMinutes(5)
  }
  
  def findAllInLastMinutes(min : Int) : Seq[Instant] = {
    val date : java.sql.Date = new java.sql.Date(this.getTimeBeforeGivenMinutes(min).getTime())
    DB.withConnection { implicit connection =>
      SQL(Instant.queryUniques + " WHERE created_at >= {created_at} AND is_old = 'f' ORDER BY vehicle_id, created_at DESC").on("created_at" -> date).as(Instant.tuple *)
    }  
  }
  
  def serviceStatus() : Int = {
    val date : java.sql.Date = new java.sql.Date(this.getTimeBeforeGivenMinutes(1).getTime())
    val dateFormat = DateFormat.getDateTimeInstance()

    DB.withConnection { implicit connection =>
      var query = "SELECT DISTINCT ON (vehicles.line_id) vehicles.line_id, instants.id, speed, is_old, has_highest_quality, vehicle_id, instants.created_at, ST_AsText(coordinates) AS coordinates FROM instants INNER JOIN vehicles ON (vehicles.id = instants.vehicle_id)"
      SQL(query + " WHERE instants.created_at >= {created_at} AND is_old = 'f'").on("created_at" -> date).as(Instant.tuple *).size
    }
  }
  
  def create(instant: Instant): Unit = {
    DB.withConnection { implicit connection =>
      SQL("insert into instants(speed, is_old, has_highest_quality, vehicle_id, created_at, coordinates) " +
      		"values ({speed}, {age}, {quality}, {vehicleId}, {date}, ST_SetSRID(ST_MakePoint({lon}, {lat}), 4326))").on(
        'speed -> instant.speed,
        'age -> instant.isOld,
        'quality -> instant.hasHighestQuality,
        'vehicleId -> instant.vehicleId,
        'date -> instant.createdAt,
        'lat -> instant.coordinate("lat"),
        'lon -> instant.coordinate("lon")
      ).executeUpdate()
    }
  }
  
  def insertNew(vehicleData : HashMap[String, Any]) {
    
	var vehicle = Vehicle.findByIdentifier(vehicleData("vehicleId").asInstanceOf[String].toLong)
	if(vehicle == null)
	  return

	val latitude = vehicleData("latitude").asInstanceOf[Double] 
	val longitude = vehicleData("longitude").asInstanceOf[Double]	
	val date = getDateFromParams(vehicleData("date").asInstanceOf[HashMap[String, Int]])
	val dateFormat = DateFormat.getDateTimeInstance()

	var coordinates = Map[String, Double]()
	coordinates += ("lat" -> latitude, "lon" -> longitude)
	
	val instant = Instant(NotAssigned, 
	    vehicleData("speed").asInstanceOf[Double],
	    !vehicleData("age").asInstanceOf[Boolean],
	    vehicleData("quality").asInstanceOf[Boolean],
	    vehicle.id, date, coordinates)
    this.create(instant)
    
    Logger.info("New instant recorded with date: "+ dateFormat.format(date))
  }
  
  
  
  def parseCoordinateString(coordinate : String) : Map[String, Double] = {
    val geom = new com.vividsolutions.jts.io.WKTReader().read(coordinate)
    
    geom match {
    	case point: Point => return Map("lon" -> point.getX(), "lat" -> point.getY())
    	case _ => throw new ClassCastException
    }
  }
  
  def getTimeBeforeGivenMinutes(minutes : Int) : Date = {
    var calendar : Calendar = new GregorianCalendar(timeZone)
    calendar.roll(Calendar.MINUTE, -minutes)
    return calendar.getTime()
  }
  
  def getDateFromParams(params : HashMap[String, Int]) : Date = {
    val seconds = params("seconds")
    
    var hours = scala.math.floor(seconds/3600);   
	val divMinutes = seconds % 3600;
	var minutes = scala.math.floor(divMinutes / 60);
	var divSeconds = divMinutes % 60;
    
	var calendar : Calendar = new GregorianCalendar(timeZone)
	// For java, january is 0
	var month = params("month")-1
	calendar.set(calendar.get(Calendar.YEAR), month, params("day"), hours.toInt, minutes.toInt, scala.math.ceil(divSeconds).toInt)
	return calendar.getTime()
  }
  
}
