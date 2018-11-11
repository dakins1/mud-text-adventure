package mud

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props

class NPC_Manager extends Actor {
  import NPC_Manager._
  
  def receive = {
    case NewNPC(name:String) => {
      val n = context.actorOf(Props(new NPC(name)), name)
      n ! NPC.Initialize
    }
    
    case PrintPos => 
      for (c <- context.children) c ! NPC.PrintPos

    case _ => println("Unhandled method in NPC_Manager")
  }
}

object NPC_Manager {
  case class NewNPC(name:String)
  case object PrintPos
}