package mud

import scala.io.Source

class Room(
    val name: String,
    val desc: String,
    val exits: Array[Option[Int]],
    private var _items: List[Item]
    ) {
  
  val directionArray = Array("north","south","east","west","up","down")
  def items = _items
  
  def removeItem(itemToRemove:Item):Unit = _items = _items.filter(i => i.name != itemToRemove.name)
  def addItem(itemToAdd:Item):Unit = _items ::= itemToAdd
  
  def fullDescription(): String = {
    var masterString:String = (name + "\n" + desc + "\n")
    masterString += "Available items: \n"
    if (items.size >= 1) items.foreach(s => masterString += s.name + " - " + s.desc + "\n") 
    else masterString += "None\n"   
    //for (i <- 0 to 5) {
      //if (exits(i) != None) masterString += Room.rooms(exits(i).get).name + " is to the " + directionArray(i) + "\n"
    //}
    masterString += "Exits: \n"
    for (i <- 0 to 5) {
      if (exits(i) != None) masterString += Room.rooms(exits(i).get).name + " is to the " + directionArray(i) + "\n"
    }
    //if (items.size >= 1) items.foreach(s => masterString += s.name + " - " + s.desc + "\n") 
    //else masterString += "None\n"   
    masterString
  }
  
  def getExit(dir: Int): Option[Room] = {
    //if(exits(dir) == None) None else
    //Some(Room.rooms(exits(dir).get))
    exits(dir).map(Room.rooms)
  }

}

object Room {
  val rooms = readRooms() 
  
  def readRooms(): Array[Room] = {
    val source = Source.fromFile("map.txt")
    val lines = source.getLines()
    val rooms = Array.fill(lines.next().trim.toInt)(readRoom(lines))
    source.close()
    rooms
  }
  
  def readRoom(lines: Iterator[String]): Room = {
    val name = lines.next()
    val desc = lines.next()
    val exits = lines.next().split(",").
        map(_.trim.toInt).
        map(i => if(i == -1) None else Some(i))
    val items = List.fill(lines.next().trim.toInt){
      val Array(name, desc) = lines.next().split(",", 2)
      Item(name.trim, desc.trim)
    }
    new Room(name, desc, exits, items)
  }
  
  
}