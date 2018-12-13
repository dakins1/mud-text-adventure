package mud

import akka.actor.Actor
import akka.actor.ActorRef
import scala.collection.mutable.Buffer
import java.io.PrintStream
import java.io.BufferedReader
import java.net.Socket
import scala.collection.mutable.Queue

class Player(val name: String, sock: Socket, ps: PrintStream, br: BufferedReader) extends Actor {
  import Player._
  import CharacterMessages._

  //Data Members
  private var _position: ActorRef = null
  def position = _position
  private val fists = Item((name + "'s_fisticuffs"), (name + "'s bare fists."), 10, 5, 10)
  private var _items = Buffer.empty[Item] += fists
  def items = _items
  private var _health = 100
  def health = _health
  private var equippedItem: Item = fists
  private var victim: ActorRef = null.asInstanceOf[ActorRef]
  private var inCombat: Boolean = false
  private def itemDamage = {
    if (equippedItem != null) {
      equippedItem.baseAttack + util.Random.nextInt(equippedItem.maxAttack - equippedItem.baseAttack)
    } else 0
  }
  private def itemSpeed = {
    if (equippedItem != null) equippedItem.speed
    else 0
  }
  private var roomInfo:Map[String,String] = null

  val directionArray = List("north", "south", "east", "west", "up", "down")

  def receive = {
    case Initialize =>
      ps.print(hello)
      ps.print(instructions)
      ps.println("Press enter to continue.")
      br.readLine()
      Server.roomManager ! RoomManager.GetRandomRoom(self)
      Server.roomManager ! RoomManager.GetRoomInfo
      val msg = name + " has joined the game \n"
      Server.playerManager ! PlayerManager.SendMessage(msg, null, false)

    case TakeRoomInfo(info) =>
      roomInfo = info
      
    case CheckInput =>
      if (br.ready()) {
        val input = br.readLine()
        processCommand(input)
      }

    case TakeExit(roomOp, dir) => {
      if (roomOp != None) {
        val msg1 = name + " has gone " + directionArray(dir) + "\n"
        Server.playerManager ! PlayerManager.SendMessage(msg1, position, true) //send msg to server
        position ! Room.RemoveFromRoom(self)
        _position = roomOp.get
        position ! Room.AddToRoom(self)
        roomOp.get ! Room.PrintDescription //should send message to the player
        val msg2 = name + " has entered the room\n"
        Server.playerManager ! PlayerManager.SendMessage(msg2, position, true) //send msg to server
      } else if (!inCombat) ps.println("Leon's poor map building skills prevent you from going this way.\n")
    }

    case PrintRoom(roomDesc) =>
      ps.println(roomDesc) //should print room desc...

    case PrintMessage(str, room, roomSpecial) =>
      if (roomSpecial) {
        if (room == position) ps.println(str)
      } else ps.println(str)

    case TakeShortestPath(queue) =>
      ps.println("Shortest path: ")
      while (queue.size > 1) ps.print(queue.dequeue() + ", ")
      ps.println(queue.dequeue)
      
    case TakeItem(itemOp) => {
      if (itemOp != None) {
        _items += itemOp.get
        ps.println(itemOp.get.name + " was added to your inventory\n")
        val msg = name + " has grabbed the " + itemOp.get.name + "\n"
        Server.playerManager ! PlayerManager.SendMessage(msg, position, true)
      } else ps.println("that item is not here.\n")
    }

    case CharacterMessages.StartKill(actorOp) =>
      if (actorOp != None) {
        inCombat = true
        victim = actorOp.get
        Server.activityManager ! ActivityManager.ScheduleActivity(
          itemSpeed, CharacterMessages.Kill, self)
      } else ps.println("That player is not in this room.\n")

    case CharacterMessages.Kill =>
      if (/*victim != null &&*/  inCombat && health>0) {
        ps.println("You attack " + victim.path.name + "!\n")
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
        ps.println(victim.path.name + " attacked you!")
        ps.println("Damage dealt: " + damage + ", Your health: " + health + "\n")
        if (health > 0) {
          if (inCombat) Server.activityManager ! ActivityManager.ScheduleActivity(itemSpeed, CharacterMessages.Kill, self)
        } else {
          inCombat = false
          victim ! CharacterMessages.EndCombat(Died())
          victim = null.asInstanceOf[ActorRef]
          ps.println("You died. Sowwy!")
        }
      } else { 
        sender ! CharacterMessages.EndCombat(NotInRoom())
        //victim = null.asInstanceOf[ActorRef]
      }

    case EndCombat(result) => {
      inCombat = false
      if (victim != null) {
        result match {
          case _: Died => ps.println(sender.path.name + " died.\n")
          case _: NotInRoom => ps.println(sender.path.name + " is no longer in the room.\n")
          case _: Fled => ps.println(sender.path.name + " has fled!\n")
          case _ => ps.println("Unhandled message in EndCombat match")
        }
      victim = null.asInstanceOf[ActorRef]
      }
    }

    case CharacterMessages.AssignStartingRoom(startPos) => {
      _position = startPos
      position ! Room.AddToRoom(self)
      val msg = name + " has entered the room\n"
      Server.playerManager ! PlayerManager.SendMessage(msg, position, true) //send msg to server
      position ! Room.PrintDescription
    }
    
    case PrintPrivateMsg(msg) =>
      ps.println(msg)
      ps.println()
      
    case m => println("Unhandled message in Player")
  }

  def processCommand(command: String): Unit =
    command.split(" ")(0) match {
      case "north" => move(0)
      case "south" => move(1)
      case "east" => move(2)
      case "west" => move(3)
      case "up" => move(4)
      case "down" => move(5)
      case "flee" => flee()
      case "say" => processSay(command)
      case "tell" => processTell(command)
      case "look" => position ! Room.PrintDescription
      case "drop" if (command.split(" ").size == 2) => dropInv(command.split(" ")(1))
      case "grab" if (command.split(" ").size == 2) => addInv(command.split(" ")(1))
      case "i" => printInfo()
      case "equip" if (command.split(" ").size == 2) => equip(command.split(" ")(1))
      case "equipped" => printEquip()
      case "unequip" => unequip()
      case "kill" if (command.split(" ").size == 2) => inquireKill(command.split(" ")(1))
      case "shortestPath" if (command.split(" ").size == 2) => shortestPath(command.split(" ")(1))
      case "reset" => _health = 100
      case "help" => ps.println(instructions)
      case _ => ps.println("??????????? \n ???? ??? \n??? ?????????? ? \n       ???\n?\n")
    }

/***Helper functions***/

  def shortestPath(dest:String):Unit = {
    Server.roomManager ! RoomManager.GetShortestPath(position.path.name, dest)
  }
  
  def processTell(command: String) = {
    val addressee = command.split(" ")(1)
    val msg = name + " tells you: " + command.takeRight(command.size - addressee.size - "tell  ".size) + "\n"
    Server.playerManager ! PlayerManager.SendPrivateMessage(msg, addressee)
  }

  def processSay(command: String) = {
    val dialogue = command.takeRight(command.size - "say ".size) + "\n"
    val msg = name + " says: " + dialogue
    Server.playerManager ! PlayerManager.SendMessage(msg, position, true)
    ps.println("You said: " + dialogue)
  }

  def dropInv(itemToDrop: String): Unit = {
    val itemOp = items.find(i => (i.name == itemToDrop))
    if (itemOp != None) {
      _items -= itemOp.get
      ps.println("You dropped " + itemOp.get.name + " from your inventory.\n")
      if (equippedItem == itemOp.get) unequip()
      position ! Room.DropItem(itemOp.get)
      val msg = name + " dropped the " + itemOp.get.name + "\n"
      Server.playerManager ! PlayerManager.SendMessage(msg, position, true)
    } else ps.println("You don't have that in your inventory.\n")
  }

  //Equipment and inventory

  def addInv(itemName: String): Unit = {
    position ! Room.GetItem(itemName)
  }

  def equip(itemName: String): Unit = {
    val i = items.find(s => s.name == itemName)
    if (i != None) {
      equippedItem = i.get
      ps.println("You have equipped " + i.get.name + "\n")
    }
  }

  /*
   * Fists will automatically be equipped after unequipping an item,
   * But if user unequips fists, then they will have nothing equipped
   */
  def unequip() {
    if (equippedItem != null) {
      if (items.contains(fists)) {
        ps.println("You unequipped " + equippedItem.name + "\n")
        if (equippedItem == fists) equippedItem = null.asInstanceOf[Item]
        else equippedItem = fists
      } else {
        ps.println("You unequipped " + equippedItem.name + ", and now you are defenseless.\n")
        equippedItem = null.asInstanceOf[Item]
      }
    } else ps.println("You have nothing to unequip. You were and still are defenseless.\n")
  }

  def printEquip() {
    if (equippedItem != null) ps.println(equippedItem.name + " is equipped\n")
    else ps.println("You have nothing equipped. You are defenseless!\n")
  }

  def printInfo():Unit = {
    ps.println("Health: " + health)
    printEquip()
    var inventory = ""
    for (i <- _items) inventory += (i.name + " - " + i.desc + "\n" +
      "    speed: " + i.speed + ", attack range: " + i.baseAttack + "-" + i.maxAttack + "\n")
    ps.println(inventory)
  }

  //Killing

  def inquireKill(victimName: String): Unit = {
    position ! Room.Attendance(victimName)
  }

  //Movement
  
  def move(dir: Int): Unit = { //make this send a message asking for a room
    if (!inCombat) position ! Room.GetExit(dir)
    else ps.println("Nice try\n")
  }
  
  //TODO
  def flee():Unit = {
    if (util.Random.nextInt(3) == 0) {
      victim ! EndCombat(Fled())
      val tmp = position
      for (i <- 0 to 5)
        position ! Room.GetExit(i)
      inCombat = false
    } else ps.println("Leon's poor map building skills prevent you from fleeing!")
  }

  //Game info

  val hello = "\n\nHello, and welcome to my mud. \\|^.^|/ \n"
  val instructions = """-To move around, type in a direction (i.e. north, south, east, etc.).
-To pick up an item, type "grab" and then the name of the item. 
-To drop an item, type "drop" and then the name of the item. 
-To equip an item, type "equip" and then the name of the item.
-Note: The lower your item's speed number is, the faster it actually is. 
-To see what item you have equipped, type "equipped".
-To unequip your equipped time, type "unequip". If your fists are
  still in your inventory, those will be equipped by default. 
-Speaking of inventory, to view your inventory, simply type "i".
  This will also show you your health.
-To look around your current room, type "look".
-To send a message to your room, type "say " followed by your message.
-To send a private message, type "tell ", the recipients exact name, 
  then the message. You cannot talk to NPCs. But you can talk to yourself. 
-To initiate an attack, type "kill " followed by your victim's name.
-If you realize attacking was a mistake, you can try to flee. To do so,
  just type "flee". There is a slim chance it will work, but you can try
  to flee as many times as your typing speed will allow.
-If you die, type "reset" to put your health back to 100. 
-To find the shortest path to a location, type "shortestPath" followed
  by the exact name of the destination. 
-To repeat these instructions, type "help".
-Have fun!

"""

}
object Player {
  //Messages from God knows where
  case object CheckInput
  case class PrintPrivateMsg(msg:String)
  
  //RoomManager
  case class TakeShortestPath(path:Queue[String])
  case class TakeRoomInfo(info:Map[String, String])
  
  
  //Messages from room
  case class PrintRoom(roomDesc: String)
  case class TakeItem(itemOp: Option[Item])

  //Messages from PlayerManager
  case class AssignStartingRoom(startPos: ActorRef)
  case class PrintMessage(str: String, room: ActorRef, roomSpecial: Boolean)
  case object Initialize
}
