package mud

import scala.io.Source
import akka.actor.Actor
import akka.actor.ActorRef
import scala.collection.mutable.Buffer

//Mine

class Room(
  val keyword: String,
  val name: String,
  val desc: String,
  val exitKeys: Array[String],
  private var _items: Buffer[Item]) extends Actor {
  private var exits: Array[Option[ActorRef]] = null
  
  import Room._
  
  def receive = {

    case GetExit(dir) =>
      sender ! Player.TakeExit(getExit(dir))
      
    case GetItem(itemName: String) => {
      val i = items.find(i => i.name == itemName)
      sender ! Player.TakeItem(i)
      if (i != None) _items -= i.get
    }
    
    case DropItem(item:Item) =>
      _items += item
      
    case PrintDescription =>
      sender ! Player.PrintRoom(fullDescription())
      
    case LinkExits(rooms) =>
      exits = exitKeys.map(key => rooms.get(key))
      
    case m => 
      println("Unhandled message in Room: " + m)
  }

  val directionArray = Array("north", "south", "east", "west", "up", "down")
  def items = _items

  def removeItem(itemToRemove: Item): Unit = _items = _items.filter(i => i.name != itemToRemove.name)
  def addItem(itemToAdd: Item): Unit = _items += itemToAdd

  def fullDescription(): String = { //TODO fix later
    var masterString: String = (name + "\n" + desc + "\n")
    masterString += "Available items: \n"
    if (items.size >= 1) items.foreach(s => masterString += s.name + " - " + s.desc + "\n")
    else masterString += "None\n"
    masterString += "Exits: \n"
    //for (i <- exits) {
    //  if (i != "-1") //masterString += Room.rooms(i).name + " is to the " + directionArray(exits.indexOf(i)) + '\n'
    //    //TODO gotta fix the string input here
    //}
    masterString
  }

  def getExit(dir: Int): Option[ActorRef] = {
    exits(dir)
  }

}

object Room {
  //Messages sent by Players
  case class GetExit(dir: Int)
  case object PrintDescription // Doesn't print here. Tells player to print.
  case class GetItem(itemName: String)
  case class DropItem(item: Item)
  
  //Sent by RoomManager
  case class LinkExits(rooms: Map[String, ActorRef])
}