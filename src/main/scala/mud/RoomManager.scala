package mud

import akka.actor.Actor
import scala.io.Source
import akka.actor.ActorRef
import akka.actor.Props
import scala.util.Random
import scala.collection.concurrent.TrieMap
import scala.collection.mutable.Queue
import scala.collection.mutable.Set


class RoomManager extends Actor {
  import RoomManager._

  //Data Members
  private var roomsInfo: TrieMap[String, Array[String]] = TrieMap.empty[String, Array[String]]
  private var roomKeywords: TrieMap[String, String] = TrieMap.empty[String, String]
  private var keywordsFlipped: TrieMap[String, String] = TrieMap.empty[String, String]
  val directionArray = List("north", "south", "east", "west", "up", "down")

  private val rooms = readRooms()
  for ((_, room) <- rooms) room ! Room.LinkExits(rooms)

  def readRooms(): BSTMap[String, ActorRef] = {
    val source = Source.fromFile("map.txt")
    val lines = source.getLines()
    val rooms = Array.fill(lines.next().trim.toInt)(readRoom(lines))
    source.close()
    new BSTMap[String, ActorRef](_<_).toBSTMap(rooms) 
  }

  def readRoom(lines: Iterator[String]): (String, ActorRef) = {
    val keyword = lines.next()
    val name = lines.next()
    val desc = lines.next()
    val exits = lines.next().split(",")
    roomsInfo += keyword -> exits
    keywordsFlipped += name -> keyword
    roomKeywords += keyword -> name
    val items = List.fill(lines.next().trim.toInt) {
      val Array(name, desc) = lines.next().split(",", 2)
      val Array(speed, baseAttack, maxAttack) = lines.next().split(",")
      Item(name.trim, desc.trim, speed.trim.toInt, baseAttack.trim.toInt, maxAttack.trim.toInt)
    }.toBuffer
    keyword -> context.actorOf(Props(new Room(keyword, name, desc, exits, items)), name)
  }

  def helper(room: String, dest: String, roomsInfo: TrieMap[String, Array[String]], visited: Set[String] = Set.empty[String]): Int = {
    if (room == dest) 0
    else if (visited(room)) 10000000
    else {
      visited += room
      (for (r <- roomsInfo(room); if r != "-1") yield (helper(r, dest, roomsInfo, visited))).min + 1
    }
  }

  def getPath(start: String, dest: String, roomsInfo: TrieMap[String, Array[String]]): Queue[String] = {
    val q = Queue.empty[String]
    val visited = Set.empty[String]
    var room = start
    while (room != dest) {
      val path = (for (r <- roomsInfo(room); if r != "-1"; if !visited(r)) yield (r, helper(r, dest, roomsInfo)))
      val lngth = for (t <- path) yield t._2
      val i = path.indexWhere(s => s._2 == lngth.min) //index of next room to take
      val dir = directionArray(roomsInfo(room).indexOf(path(i)._1))
      q.enqueue(dir)
      visited += room
      room = path(i)._1
    }
    q
  }

  def receive = {
    case GetRandomRoom(player) => {
      val r = rooms.toArray
      sender ! CharacterMessages.AssignStartingRoom(r(Random.nextInt(r.size))._2)
    }

    case GetRoomInfo =>
      //val info = rooms.map(r => r._1 -> r._2.path.name)
      //sender ! Player.TakeRoomInfo(info)

    case GetShortestPath(room, dest) =>
      val x = (for (r <- keywordsFlipped) yield r._1).toArray
      if (x.contains(room) && x.contains(dest)) {
        sender ! Player.TakeShortestPath(getPath(keywordsFlipped(room), keywordsFlipped(dest), roomsInfo))
      } else sender ! Player.PrintPrivateMsg("That room doesn't exist!")
      
    case TestPath(room, dest) =>
      val q = getPath(keywordsFlipped(room), keywordsFlipped(dest), roomsInfo)
      while (!q.isEmpty) println(q.dequeue)

    case SendExits(name, keyword, exits) =>
    //roomsInfo += keyword -> exits

    case TestRoomsInfo =>
      roomsInfo.foreach(s => println(s._1))

    case m => println("Unhandled message in RoomManager: " + m)
  }
}

object RoomManager {
  case object TestRoomsInfo

  case class TestPath(room: String, dest: String)
  //From player
  case object GetRoomInfo

  //From Room
  case class GetShortestPath(room: String, dest: String)
  case class SendExits(name: String, keyword: String, exits: Array[String])
  //Messages from Player Manager
  case class GetRandomRoom(player: ActorRef)
}