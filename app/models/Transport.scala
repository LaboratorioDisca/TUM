package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._

case class Transport(id : Long, name : String)

object Transport {
 
  def findAll() : Seq[Transport] = {
    DB.withConnection { implicit connection =>
      SQL(Transport.query).as(Transport.tuple *)
    }
  }
  
  def find(id : Long) : Transport = {
    DB.withConnection { implicit connection =>
      SQL(Transport.query+" WHERE id = {transport_id}").on("transport_id" -> id).as(Transport.tuple.single)
    }
  }
  val query = "SELECT id, name FROM transports"
  val tuple = {
    get[Long]("id") ~
    get[String]("name")  map {
      case id~name => 
        Transport(id, name)
    }
  }
}