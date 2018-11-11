package mud

import akka.actor.Actor
import akka.actor.ActorRef

class NPC(name:String) extends Actor {
  import NPC._
  
  private var position:ActorRef = null
  val directionArray = List("north", "south", "east", "west", "up", "down")
  
  def receive = {
    case Initialize => 
      Server.roomManager ! RoomManager.GetRandomRoom(self)
      
    case PrintPos =>
      println(self.path.name + ": " + position.path.name)
      
    case Schedule(count) =>
      Server.activityManager ! ActivityManager.ScheduleActivity(count + util.Random.nextInt(10), NPC.Move, self)      
      
    case CharacterMessages.AssignStartingRoom(startPos) =>
      position = startPos
      startPos ! Room.AddToRoom(self)

    case Move => 
      position ! Room.GetExit(util.Random.nextInt(6)) 
      
    case CharacterMessages.TakeExit(roomOp, dir) =>
      if (roomOp != None) {
        val msg1 = name + " has gone " + directionArray(dir) + "\n"
        println(msg1)
        Server.playerManager ! PlayerManager.SendMessage(msg1, position, true) //send msg to server
        position ! Room.RemoveFromRoom(self)
        position = roomOp.get
        position ! Room.AddToRoom(self)
        val msg2 = name + " has entered the room\n"
        Server.playerManager ! PlayerManager.SendMessage(msg2, position, true) //send msg to server
      }
    
    case m => 
      println("unhandled message in NPC" + m.toString())
  }
}

object NPC {
  case class AssignStartingRoom(startPos:ActorRef)
  case class Schedule(count:Int)
  case object Initialize
  case object PrintPos
  case object Move
}