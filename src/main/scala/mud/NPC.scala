package mud

import akka.actor.Actor
import akka.actor.ActorRef

class NPC(name: String, private var _health:Int) extends Actor {
  import CharacterMessages._
  import NPC._

  //Data Members
  def health = _health
  private var inCombat = false
  private var victim:ActorRef = null.asInstanceOf[ActorRef]
  private val fists = Item((name + "'s_fisticuffs"), (name + "'s bare fists."), 10, 5, 10)
  private var equippedItem = fists
  private def itemDamage = {
    if (equippedItem != null) {
      equippedItem.baseAttack + util.Random.nextInt(equippedItem.maxAttack - equippedItem.baseAttack)
    } else 0
  }
  private def itemSpeed = {
    if (equippedItem != null) equippedItem.speed
    else 0
  }
  
  private var position: ActorRef = null
  val directionArray = List("north", "south", "east", "west", "up", "down")

  def receive = {
    case Initialize =>
      Server.roomManager ! RoomManager.GetRandomRoom(self)

    case PrintPos =>
      println(self.path.name + ": " + position.path.name)

    case Schedule =>
      Server.activityManager ! ActivityManager.ScheduleActivity(util.Random.nextInt(10), NPC.Move, self)

    case CharacterMessages.AssignStartingRoom(startPos) =>
      position = startPos
      startPos ! Room.AddToRoom(self)

    case Move =>
      if (!inCombat) position ! Room.GetExit(util.Random.nextInt(6))
      else if (util.Random.nextInt(1) == 0) {
        inCombat = false
        position ! Room.GetExit(util.Random.nextInt(6))
        victim ! EndCombat(Fled())
      }
      
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
        Server.activityManager ! ActivityManager.ScheduleActivity(util.Random.nextInt(250), NPC.Move, self)
      } else Server.activityManager ! ActivityManager.ScheduleActivity(util.Random.nextInt(150), NPC.Move, self)

    case CharacterMessages.StartKill(actorOp) =>
      if (actorOp != None) {
        inCombat = true
        victim = actorOp.get
        Server.activityManager ! ActivityManager.ScheduleActivity(
          itemSpeed, CharacterMessages.Kill, self)
      }
    case CharacterMessages.Kill =>
      if (/*victim != null &&*/  inCombat && health>0) {
        //victim ! VictimAssignment //don't know about this one
        victim ! CharacterMessages.Attack(itemDamage, position)
        Server.activityManager ! ActivityManager.ScheduleActivity(itemSpeed, CharacterMessages.Kill, self)
      }

    case VictimAssignment =>
      victim = sender
      
    case CharacterMessages.Attack(damage, room) =>
      if (/*victim != null &&*/ room == position && health>0) {
        inCombat = true
        victim = sender
        _health -= damage
        if (health > 0) {
          if (inCombat) Server.activityManager ! ActivityManager.ScheduleActivity(itemSpeed, CharacterMessages.Kill, self)
        } else {
          inCombat = false
          victim ! CharacterMessages.EndCombat(Died())
          victim = null.asInstanceOf[ActorRef]
        }
      } else { 
        sender ! CharacterMessages.EndCombat(NotInRoom())
        //victim = null.asInstanceOf[ActorRef]
      }

    case EndCombat(result) => {
      inCombat = false
      victim = null.asInstanceOf[ActorRef]
    }
      
      
    case m =>
      println("unhandled message in NPC" + m.toString())
  }
}

object NPC {
  case class AssignStartingRoom(startPos: ActorRef)
  case object Schedule
  case object Initialize
  case object PrintPos
  case object Move
}