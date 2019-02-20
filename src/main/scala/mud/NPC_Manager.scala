package mud

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props

class NPC_Manager extends Actor {
  import NPC_Manager._
  
  def receive = {
    case NewNPC(name:String, health:Int) => {
      val n = context.actorOf(Props(new NPC(name, health)), name)
      n ! NPC.Initialize
    }
    
    case StartMovement =>
      for (c <- context.children) c ! NPC.Schedule
    
    case PrintPos => 
      for (c <- context.children) c ! NPC.PrintPos

    case _ => println("Unhandled method in NPC_Manager")
  }
}

object NPC_Manager {
  case class NewNPC(name:String, health:Int)
  case object StartMovement
  case object PrintPos
}