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
  
  private var memberList = Buffer.empty[ActorRef] 
  
  def receive = {

    case GetExit(dir) =>
      sender ! CharacterMessages.TakeExit(getExit(dir), dir)

    case GetItem(itemName: String) => {
      val i = items.find(i => i.name == itemName)
      sender ! Player.TakeItem(i)
      if (i != None) _items -= i.get
    }

    case DropItem(item: Item) =>
      _items += item
      
    case AddToRoom(player:ActorRef) =>
      memberList += player
      
    case RemoveFromRoom(player:ActorRef) =>
      memberList -= player
      
    case Attendance(name:String) =>
      sender ! CharacterMessages.StartKill(memberList.find(i => i.path.name == name))
      
      
    case PrintDescription =>
      {
        var masterString: String = ("\n " + name + "\n" + desc + "\n")
        masterString += "Available items: \n"
        if (items.size >= 1) items.foreach(s => masterString += s.name + " - " + s.desc + "\n" + 
            "     speed: " + s.speed + ", attack range: " + s.baseAttack + "-" + s.maxAttack + "\n")
        else masterString += "None\n"
        masterString += "Players here: \n"
        if (memberList.size >= 1) memberList.foreach(s => masterString += "    " + s.path.name.toString() + "\n")
        else masterString += "None\n"
        masterString += "Exits: \n"
        for (e <- exits) {
          if (e != None) {
            masterString += "    " + e.get.path.name.toString() + " is to the " + directionArray(exits.indexOf(e)).toString() + '\n'
            //Server.roomManager ! RoomManager.PrintExitsRequest(e, sender) //directionArray(exits.indexOf(i)) + '\n'
          }
        }
        sender ! Player.PrintRoom(masterString)
      }
    case LinkExits(rooms) =>
      exits = exitKeys.map(key => rooms.get(key))

    case m =>
      println("Unhandled message in Room: " + m)
  }

  val directionArray = List("north", "south", "east", "west", "up", "down")
  def items = _items

  def removeItem(itemToRemove: Item): Unit = _items = _items.filter(i => i.name != itemToRemove.name)
  def addItem(itemToAdd: Item): Unit = _items += itemToAdd

  def getExit(dir: Int): Option[ActorRef] = {
    exits(dir)
  }

}

object Room {
  //Messages sent by Players
  case class AddToRoom(player:ActorRef)
  case class RemoveFromRoom(player:ActorRef)
  case class GetExit(dir: Int)
  case object PrintDescription // Doesn't print here. Tells player to print.
  case class GetItem(itemName: String)
  case class DropItem(item: Item)
  case class Attendance(name:String)

  //Sent by RoomManager
  case class LinkExits(rooms: Map[String, ActorRef])

}