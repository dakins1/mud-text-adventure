package mud

import akka.actor.Actor
import akka.actor.ActorRef

class ActivityManager extends Actor {
  import ActivityManager._
  
  def receive = {
    case CheckQueue => 
      
    case _ =>
      println("Unhandled message in ActivityManager")
  }
}

object ActivityManager {
  case object CheckQueue
}