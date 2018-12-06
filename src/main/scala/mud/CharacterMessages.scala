package mud

import akka.actor.ActorRef

object CharacterMessages {

  case class AssignStartingRoom(startPos: ActorRef)

  case class TakeExit(roomOp: Option[ActorRef], dir: Int)

  case object Kill

  case class TellToScheduleKill(count: Int)

  case class StartKill(victim: Option[ActorRef])
  
  case class Attack(damage:Int, room:ActorRef)
  
  case class InitialAttack(attacker:ActorRef, damage:Int, attackerRoom:ActorRef)

  case class EndCombat(result:Any)
  
  case object VictimAssignment
  
  case class NotInRoom()
  case class Died()
  case class Fled()
  
}