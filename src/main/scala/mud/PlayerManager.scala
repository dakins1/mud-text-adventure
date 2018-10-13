package mud

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props

class PlayerManager extends Actor {
  import PlayerManager._
  def receive = {
    case NewPlayer(name) => {
      val p = context.actorOf(Props(new Player(name)), name)
      Main.roomManager ! RoomManager.GetRandomRoom(p)
    }

    case CheckAllInput =>
      for (child <- context.children) child ! Player.CheckInput

    case TakeStartingRoom(startPos, player) =>
      player ! Player.AssignStartingRoom(startPos)

  }
}

object PlayerManager {
  //Messages from Room Manager
  case class TakeStartingRoom(startPos: ActorRef, player: ActorRef)

  //Messages from Main
  case class NewPlayer(name: String)
  case object CheckAllInput
}