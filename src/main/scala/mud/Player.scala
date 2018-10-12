package mud

import akka.actor.Actor
import akka.actor.ActorRef
import scala.collection.mutable.Buffer

class Player (val name:String) extends Actor {
  import Player._
  
  //Data Members
  private var _position:ActorRef = null //maybe change later...feels dirty
  def position = _position
  private var _items = Buffer.empty[Item] 
  def items = _items
  val ps = Console.out
  
  def receive = {
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
    
    case TakeStartingRoom(startPos) =>
      _position = startPos
  }
  
  
  def processCommand(command:String):Unit = command.split(" ")(0) match {
    case "north" => move(0)
    case "south" => move(1)
    case "east"  => move(2)
    case "west"  => move(3)
    case "up"    => move(4)
    case "down"  => move(5)
    case "look"  => position ! Room.PrintDescription
    case "drop"  => dropFromInventory(command.split(" ")(1))
    case "grab" if (command.split(" ").size == 2)  => addInv(command.split(" ")(1))
    case "i"     => println(inventoryListing)
    case _       => println("??????????? \n ???? ??? \n??? ?????????? ? \n       ???\n?\n")
  }
  
  
  def dropFromInventory(itemToDrop:String):Unit = {
    val itemOp = items.find(i  => (i.name == itemToDrop))
    if (itemOp != None) {
      _items -= itemOp.get 
      println("You dropped " + itemOp.get.name + " from your inventory.\n")
      position ! Room.DropItem(itemOp.get)
    }
    else println("You don't have that in your inventory.\n")
  }
  
  
  def addInv(itemName:String):Unit = {
    position ! Room.GetItem(itemName)    
  }
  
  
  def inventoryListing:String = {
    var inventory = ""
    for (i <- _items) inventory += (i.name + " - " + i.desc + "\n")
    inventory
  }
  
  def move(dir:Int):Unit = { //make this send a message asking for a room
    position ! Room.GetExit(dir)    
  }
}
object Player {
  //Messages from room
  case class TakeStartingRoom(startPos:ActorRef)
  case class PrintRoom(roomDesc:String)
  case class TakeExit(roomOp:Option[ActorRef])
  case class TakeItem(itemOp:Option[Item])

}
