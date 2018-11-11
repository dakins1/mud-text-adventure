package mud

import akka.actor.ActorRef

object CharacterMessages {
  
  case class AssignStartingRoom(startPos: ActorRef)
  
  case class TakeExit(roomOp: Option[ActorRef], dir: Int)
  
  

}