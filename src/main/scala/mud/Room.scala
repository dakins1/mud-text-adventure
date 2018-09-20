package mud

import scala.io.Source

class Room(
    val keyword:String,
    val name: String,
    val desc: String,
    val exits: Array[String],
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
    for (i <- exits) {
      if (i != "-1") masterString += Room.rooms(i).name + " is to the " +  directionArray(exits.indexOf(i)) + '\n'
    }
    
    /*
    for (i <- 0 to 5) {
      if (exits(i) != -1) masterString += Room.rooms().name + " is to the " + directionArray(i) + "\n"
    }
    //if (items.size >= 1) items.foreach(s => masterString += s.name + " - " + s.desc + "\n") 
    //else masterString += "None\n"   
     * 
     */
    masterString
  }
  
  def getExit(dir: Int): String = {
    exits(dir)
    
    
    //if(exits(dir) == None) None else
    //Some(Room.rooms(exits(dir).get))
    //Room.rooms.get(exits(dir))
    //exits(dir).map(Room.rooms)
  }

}

object Room {
  val rooms = readRooms() 
  
  def readRooms(): Map[String, Room] = {
    val source = Source.fromFile("map.txt")
    val lines = source.getLines()
    val rooms = Array.fill(lines.next().trim.toInt)(readRoom(lines))
    source.close()
    rooms.toMap
  }
  
  def readRoom(lines: Iterator[String]): (String,Room) = {
    val keyword = lines.next()
    val name = lines.next()
    val desc = lines.next()
    val exits = lines.next().split(",")
    val items = List.fill(lines.next().trim.toInt){
      val Array(name, desc) = lines.next().split(",", 2)
      Item(name.trim, desc.trim)
    }
    keyword -> new Room(keyword, name, desc, exits, items)
  }
  
  
}