package mud

import akka.actor.Actor
import akka.actor.ActorRef
import scala.collection.mutable.Buffer
import java.io.PrintStream
import java.io.BufferedReader
import java.net.Socket

class Player(val name: String, sock: Socket, ps: PrintStream, br: BufferedReader) extends Actor {
  import Player._
  import CharacterMessages._

  //Data Members
  private var _position: ActorRef = null
  def position = _position
  private var _items = Buffer.empty[Item]
  def items = _items
  val directionArray = List("north", "south", "east", "west", "up", "down")

  def receive = {
    case Initialize =>
      ps.print(hello)
      ps.print(instructions)
      br.readLine()
      Server.roomManager ! RoomManager.GetRandomRoom(self)
      val msg = name + " has joined the game \n"
      Server.playerManager ! PlayerManager.SendMessage(msg, null, false)

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
      } else ps.println("Leon's poor map building skills prevent you from going this way.\n")
    }

    case PrintRoom(roomDesc) =>
      ps.println(roomDesc) //should print room desc...

    case PrintMessage(str, room, roomSpecial) =>
      if (roomSpecial) {
        if (room == position) ps.println(str)
      } else ps.println(str)

    case TakeItem(itemOp) => {
      if (itemOp != None) {
        _items += itemOp.get
        ps.println(itemOp.get.name + " was added to your inventory\n")
        val msg = name + " has grabbed the " + itemOp.get.name + "\n"
        Server.playerManager ! PlayerManager.SendMessage(msg, position, true)
      } else ps.println("that item is not here.\n")
    }

    case CharacterMessages.AssignStartingRoom(startPos) => {
      _position = startPos
      position ! Room.AddToRoom(self)
      val msg = name + " has entered the room\n"
      Server.playerManager ! PlayerManager.SendMessage(msg, position, true) //send msg to server
      position ! Room.PrintDescription
    }

    case m => println("Unhandled message in Player")
  }

  def processCommand(command: String): Unit = command.split(" ")(0) match {
    case "north" => move(0)
    case "south" => move(1)
    case "east" => move(2)
    case "west" => move(3)
    case "up" => move(4)
    case "down" => move(5)
    case "say" => processSay(command)
    case "tell" => processTell(command)
    case "look" => position ! Room.PrintDescription
    case "drop" if (command.split(" ").size == 2) => dropInv(command.split(" ")(1))
    case "grab" if (command.split(" ").size == 2) => addInv(command.split(" ")(1))
    case "i" => ps.println(inventoryListing)
    case _ => ps.println("??????????? \n ???? ??? \n??? ?????????? ? \n       ???\n?\n")
  }

  //Helper functions

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
      position ! Room.DropItem(itemOp.get)
      val msg = name + " dropped the " + itemOp.get.name + "\n"
      Server.playerManager ! PlayerManager.SendMessage(msg, position, true)
    } else ps.println("You don't have that in your inventory.\n")
  }

  def addInv(itemName: String): Unit = {
    position ! Room.GetItem(itemName)
  }

  def inventoryListing: String = {
    var inventory = ""
    for (i <- _items) inventory += (i.name + " - " + i.desc + "\n")
    inventory
  }

  def move(dir: Int): Unit = { //make this send a message asking for a room
    position ! Room.GetExit(dir)
  }

  //Game info

  val hello = "\n\nHello, and welcome to my mud. \\|^.^|/ \n"
  val instructions = """-To move around, type in a direction (i.e. north, south, east, etc.).
-To pick up an item, type "grab" and then the name of the item. 
-To drop an item, type "drop" and then the name of the item. 
-To view your inventory, simply type "i".
-To look around your current room, type "look".
-If you want to quit, type "quit".
-Make sure to keep all commands lowercase. Have fun.

Press enter to continue

"""

}
object Player {
  //Messages from God knows where
  case object CheckInput

  //Messages from room
  case class PrintRoom(roomDesc: String)
  case class TakeItem(itemOp: Option[Item])

  //Messages from PlayerManager
  case class AssignStartingRoom(startPos: ActorRef)
  case class PrintMessage(str: String, room: ActorRef, roomSpecial: Boolean)
  case object Initialize
}
