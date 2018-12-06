package mud

import akka.actor.Actor
import scala.io.Source
import akka.actor.ActorRef
import akka.actor.Props
import scala.util.Random

//Mine

class RoomManager extends Actor {
  import RoomManager._
  
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
      val Array(speed, baseAttack, maxAttack) = lines.next().split(",")
      Item(name.trim, desc.trim, speed.trim.toInt, baseAttack.trim.toInt, maxAttack.trim.toInt)
    }.toBuffer
    println(name)
    keyword -> context.actorOf(Props(new Room(keyword, name, desc, exits, items)), name)
  }
  
  def receive = { 
    case GetRandomRoom(player) => {
      val r = rooms.toArray
      sender ! CharacterMessages.AssignStartingRoom(r(Random.nextInt(r.size))._2)
      

    }
    case m => println("Unhandled message in RoomManager: "+m)
  }
}

object RoomManager {
  
  //Messages from Player Manager
  case class GetRandomRoom(player:ActorRef)
}