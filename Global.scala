import play.api._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.util.duration._
import akka.actor.Props
import java.util.concurrent.TimeUnit

import actors.Listener

object Global extends GlobalSettings {
	
  override def onStart(app: Application) {
    
    val vehicleListener = Akka.system.actorOf(Props[Listener], name = "vehicleListener")
    
	Akka.system.scheduler.schedule(0 milliseconds, 1 second, vehicleListener, Listener.VehiclePositionChange);		  
  }
  
}