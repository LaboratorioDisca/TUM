package actors
import akka.actor._
import models.Instant
import extractors.VehicleUpdater
import play.api.Logger

class Listener extends Actor {
  
  def receive = {
    case Listener.VehiclePositionChange =>
	  Logger.info("Instant received")
      Instant.insertNew(VehicleUpdater.catchNextReport())
  }
  
}

object Listener {
  val VehiclePositionChange = "positionChange"
}
