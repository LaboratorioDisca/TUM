package extractors
import java.net.{DatagramPacket, InetAddress, MulticastSocket, SocketTimeoutException}
import scala.collection.mutable.HashMap

object VehicleUpdater {
  private val socket = new MulticastSocket(6005) 
  private val buffer = Array.ofDim[Byte](25)
  
  def parseByteData(b : Byte) : String = {
    var parsed = Integer.toString(b & (0xff), 16)
    
	if(parsed.length() < 2)
		parsed = "0"+parsed;    
    parsed
  }
  
  def catchNextReport() : HashMap[String, Any] = {
    val receivedPacket = new DatagramPacket(buffer, buffer.length)
    
    try{
        socket.receive(receivedPacket)
        val byteData:Array[Byte] = receivedPacket.getData()
        
        val byteStore = HashMap.empty[Int, String]
        
        // common code for processing coordinates
        def processCoord(coordPoints: List[Int]): String = {
          var comp = coordPoints.foldLeft(new String)((accum,n) => accum + byteStore(n))
          comp = comp.drop(1).dropRight(1)
          comp = comp.substring(0,2)+"."+comp.substring(2, comp.length())
          comp
        } 
        
        byteData.zipWithIndex.foreach {
          case(b, idx) => byteStore += (idx+1 -> parseByteData(b))
        }
        
        /** Bit indexes for reconstructing components **/
        val vehicleIdG = List(3,4,5,6,7,8)
        val vehicleId = vehicleIdG.foldLeft(new String)((accum,n) => accum + byteStore(n))
        
        // Month and day values
        val monthDayG = List(9,10)
        val monthDay = monthDayG.foldLeft(new String)((accum,n) => accum + byteStore(n))
        val day = monthDay.substring(0,2).toInt
        val month = monthDay.substring(3,4).toInt
        
        // to have chopped it's last character of last bit
        val secondsG = List(11,12,13)
        var seconds = secondsG.foldLeft(new String)((accum,n) => accum + byteStore(n))
        seconds = seconds.dropRight(1)
        
        // [both] to have chopped it's first character of it's  
        // first bit and the last character of it's last bit
        var latitude = processCoord(List(13,14,15,16))
        var longitude = "-"+processCoord(List(16,17,18,19))
        
        // to have chopped it's first character 
        var decompositionGroup1 = Integer.toString(Integer.parseInt(byteStore(19).drop(1), 16), 2)
        if(decompositionGroup1.length() == 3) {
			decompositionGroup1="0"+decompositionGroup1
        }

        var speedH = decompositionGroup1.drop(2)
        		
        // TODO: Check correctness
        var quality = decompositionGroup1.dropRight(3) == "1"
        var speed = speedH+byteStore(20)
        var age = decompositionGroup1.dropRight(2).drop(1) == "1"
        
        // to have chopped it's last character
        // var decompositionGroup2 = 21
        // to have chopped it's first character of it's first bit
        // and it's last character of it's last bit
        // var direction = List(21,22)
        
        // to have chopped it's first character
        //var decompositionGroup3 = Integer.toString(Integer.parseInt(byteStore(22).drop(1), 16), 2)       
        
        val date = new HashMap[String,Int]()
        date += ("month" -> month, "day" -> day, "seconds" -> seconds.toInt)
        
        val record = HashMap.empty[String, Any]
        record += ("vehicleId" -> vehicleId)
        record += ("latitude" -> latitude.toDouble)
        record += ("longitude" -> longitude.toDouble)
        record += ("quality" -> quality)
        record += ("age" -> age)
        record += ("speed" -> speed.toDouble)
        record += ("date" -> date)
        record += ("seconds" -> date)
        record
    }catch{
    	case e:SocketTimeoutException => return HashMap.empty[String, Any]
    }
  }
}