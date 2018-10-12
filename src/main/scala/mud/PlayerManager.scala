package mud

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props

class PlayerManager extends Actor {
  import PlayerManager._
  def receive = {
    case NewPlayer(name) =>
      context.actorOf(Props(new Player(name)), name)
      
  }
}

object PlayerManager {
  case class NewPlayer(name:String)
}