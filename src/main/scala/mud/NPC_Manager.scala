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
    
    case CreateActivity(count) =>
      for (c <- context.children) c ! NPC.Schedule(count)
    
    case PrintPos => 
      for (c <- context.children) c ! NPC.PrintPos

    case _ => println("Unhandled method in NPC_Manager")
  }
}

object NPC_Manager {
  case class NewNPC(name:String)
  case class CreateActivity(count: Int)
  case object PrintPos
}