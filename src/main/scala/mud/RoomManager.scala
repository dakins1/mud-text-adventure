package mud

import akka.actor.Actor
import scala.io.Source
import akka.actor.ActorRef
import akka.actor.Props

class RoomManager extends Actor {
  private val rooms = readRooms()
  for((_,room) <- rooms) room ! Room.LinkExits(rooms)
  
  def readRooms(): Map[String,ActorRef] = {
    val source = Source.fromFile("map.txt")
    val lines = source.getLines()
    val rooms = Array.fill(lines.next().trim.toInt)(readRoom(lines))
    source.close()
    rooms.toMap
  }
  
  def readRoom(lines: Iterator[String]): (String,ActorRef) = {
    val keyword = lines.next()
    val name = lines.next()
    val desc = lines.next()
    val exits = lines.next().split(",")
    val items = List.fill(lines.next().trim.toInt){
      val Array(name, desc) = lines.next().split(",", 2)
      Item(name.trim, desc.trim)
    }
    keyword -> context.actorOf(Props(new Room(keyword, name, desc, exits, items)), keyword)
  }
  
  def receive = {
    case m => println("Unhandled message in RoomManager: "+m)
  }
}

object RoomManager {
  
}