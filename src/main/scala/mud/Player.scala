package mud

import akka.actor.Actor
import akka.actor.ActorRef
import scala.collection.mutable.Buffer

class Player(val name: String) extends Actor {
  import Player._

  //Data Members
  private var _position: ActorRef = null //maybe change later...feels dirty
  def position = _position
  private var _items = Buffer.empty[Item]
  def items = _items
  val ps = Console.out
  val br = Console.in

  def receive = {
    case CheckInput => 
      if (br.ready()) {
        val input = br.readLine()
        processCommand(input)
      }
    
    case TakeExit(roomOp) => {
      if (roomOp != None) {
        _position = roomOp.get
        roomOp.get ! Room.PrintDescription
      } else ps.println("Leon's poor map building skills prevent you from going this way.")
    }

    case PrintRoom(roomDesc) =>
      ps.println(roomDesc)

    case TakeItem(itemOp) => {
      if (itemOp != None) {
        _items += itemOp.get
        ps.println(itemOp.get.name + " was added to your inventory")
      } else ps.println("that item is not here.")
    }

    case AssignStartingRoom(startPos) => {
      _position = startPos
      ps.println(hello)
      ps.println(instructions)
      position ! Room.PrintDescription

    }
  }

  def processCommand(command: String): Unit = command.split(" ")(0) match {
    case "north" => move(0)
    case "south" => move(1)
    case "east" => move(2)
    case "west" => move(3)
    case "up" => move(4)
    case "down" => move(5)
    case "look" => position ! Room.PrintDescription
    case "drop" if (command.split(" ").size == 2) => dropInv(command.split(" ")(1))
    case "grab" if (command.split(" ").size == 2) => addInv(command.split(" ")(1))
    case "i" => ps.println(inventoryListing)
    case _ => ps.println("??????????? \n ???? ??? \n??? ?????????? ? \n       ???\n?\n")
  }

  def dropInv(itemToDrop: String): Unit = {
    val itemOp = items.find(i => (i.name == itemToDrop))
    if (itemOp != None) {
      _items -= itemOp.get
      println("You dropped " + itemOp.get.name + " from your inventory.\n")
      position ! Room.DropItem(itemOp.get)
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

  val hello = "\n\n\nHello, and welcome to my mud. \\|^.^|/ \n"
  val instructions = """-To move around, type in a direction (i.e. north, south, east, etc.).
-To pick up an item, type "grab" and then the name of the item. 
-To drop an item, type "drop" and then the name of the item. 
-To view your inventory, simply type "i".
-To look around your current room, type "look".
-If you want to quit, type "Dillon is better than me." It has to be exactly that.
-Make sure to keep all commands lowercase. Have fun.
"""
  

}
object Player {
  //Messages from God knows where
  case object CheckInput

  //Messages from room
  case class PrintRoom(roomDesc: String)
  case class TakeExit(roomOp: Option[ActorRef])
  case class TakeItem(itemOp: Option[Item])

  //Messages from PlayerManager
  case class AssignStartingRoom(startPos: ActorRef)
}
