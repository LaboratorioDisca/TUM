package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import play.api.libs.json.Format
import play.api.libs.json.JsValue
import play.api.libs.json.JsObject
import play.api.libs.json.JsNumber
import play.api.libs.json.JsString
import play.api.libs.json.JsArray
import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.LineString

case class Line(
    id: Long, 
    name: String, 
    rightTerminal : String, 
    leftTerminal : String, 
    transportId : Long, 
    color : String, 
    simpleIdentifier : Option[String],
    paths: Seq[Seq[Map[String, Double]]]) {
  
	def pathsSize : Int = {
	  return this.paths.size;
	}
}
    
object Line {
 
  
  def findAllFromTransport(id:Long) : Seq[Line] = {
    DB.withConnection { implicit connection =>
      SQL(Line.query+" WHERE transport_id = {transport_id}").on("transport_id" -> id).as(Line.tuple *)
    }
  }
  
  def findAll() : Seq[Line] = {
    DB.withConnection { implicit connection =>
      SQL(Line.query).as(Line.tuple *)
    }
  }  
  
  def find(id: Long) : Line = {
    DB.withConnection { implicit connection =>
      SQL(Line.query+" WHERE id={line_id}").on("line_id" -> id).as(Line.tuple.single)
    }
  }
  
  val query = "SELECT * FROM lines"
  
  val tuple = {
    get[Long]("id") ~
    get[String]("name") ~ 
    get[String]("right_terminal") ~
    get[String]("left_terminal") ~
    get[Long]("transport_id") ~
    get[String]("color") ~
    get[Option[String]]("simple_identifier") map {
      case id~name~rightTerminal~leftTerminal~transportId~color~simpleIdentifier => 
        Line(id, name, rightTerminal, leftTerminal, transportId, color, simpleIdentifier, this.loadAssociatedPathsFor(id))
    }
  }
  
  def loadAssociatedPathsFor(id:Long) : Seq[Seq[Map[String, Double]]] = {
    var paths:Seq[String] = null;
    DB.withConnection { implicit connection =>
      paths = SQL("SELECT line_id, ST_AsText(content) AS path FROM ways WHERE line_id = {line_id}").on("line_id" -> id).as(str("path") *)
    }
    return paths.map(parseLinePath _)    
  }
  
  def parseLinePath(str : String) : Seq[Map[String, Double]] = {
    val geom : Geometry = new WKTReader().read(str);
    
    val pointsExtract = (line : LineString) => {
      line.getCoordinates().map( coord => Map("lon" -> coord.x, "lat" -> coord.y))
    }
    
    geom match {
    	case line: LineString => return pointsExtract(line)
    	case _ => throw new ClassCastException
    }
    return null;
  }
}