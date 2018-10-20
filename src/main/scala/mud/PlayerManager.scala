package mud

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import java.io.PrintStream
import java.io.BufferedReader
import java.net.Socket

class PlayerManager extends Actor {
  import PlayerManager._
  def receive = {
    case NewPlayer(name, sock, ps, br) => {
      val p = context.actorOf(Props(new Player(name, sock, ps, br)), name)
      p ! Player.Initialize
      //Main.roomManager ! RoomManager.GetRandomRoom(p)
      val msg = name + " has joined the game \n"
      //for(child <- context.children; if child != sender) child ! Player.PrintMessage(msg, null, false)
    }
    
    case SendMessage(str, room, roomSpecial) => {
      for(child <- context.children; if child != sender) child ! Player.PrintMessage(str, room, roomSpecial)
    }
    
    case SendPrivateMessage(msg, addressee) =>
      val c = context.children.find(c => c.path.name == addressee)
      if (c != None) {
        c.get ! Player.PrintMessage(msg, null, false)
        val response = "You told " + addressee + ": " + msg.replace(sender.path.name + " tells you: ", "")
        sender ! Player.PrintMessage(response, null, false)
      } else sender ! Player.PrintMessage("Whomst??\n", null, false)
      
    case CheckAllInput =>
      for (child <- context.children) child ! Player.CheckInput

    case TakeStartingRoom(startPos, player) =>
      player ! Player.AssignStartingRoom(startPos)
  }
}

object PlayerManager {
  //Messages from Room Manager
  case class TakeStartingRoom(startPos: ActorRef, player: ActorRef)
  
  //Messages from Player
  case class SendPrivateMessage(str:String, addressee:String)

//Messages from anywhere I guess
  case class SendMessage(str:String, room:ActorRef, roomSpecial:Boolean)
    /* If the message should only be for players in the same room as the sender, set roomSpecial to true and send the player's position.
     * Otherwise, set roomSpecial to false and set room to null
     */
  
  //Messages from Main
  case class NewPlayer(name: String, sock: Socket, ps: PrintStream, br:BufferedReader)
  case object CheckAllInput
}