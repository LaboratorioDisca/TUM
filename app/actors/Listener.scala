package actors
import akka.actor._
import models.Instant
import extractors.VehicleUpdater

class Listener extends Actor {
  
  def receive = {
    case Listener.VehiclePositionChange =>
      Instant.insertNew(VehicleUpdater.catchNextReport())
  }
  
}

object Listener {
  val VehiclePositionChange = "positionChange"
}