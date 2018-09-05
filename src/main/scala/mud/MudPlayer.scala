package mud

class Player (
    private var _position:Room,
    private var _items:List[Item]) {
  
  def position = _position
  def items = _items
  
  def processCommand(command:String):Unit = command.split(" ")(0) match {
    case "north" => move(0)
    case "south" => move(1)
    case "east"  => move(2)
    case "west"  => move(3)
    case "up"    => move(4)
    case "down"  => move(5)
    case "drop"  => getFromInventory(command.split(" ")(1))
    case "grab"  => addToInventory(???)
    case "i"     => println(inventoryListing)
    case _       => println("??????????? \n ???? ??? \n??? ?????????? ? \nTry being better???")
  }
  
  
  
  def getFromInventory(item:String):Option[Item] = ???
  def addToInventory(item:Item):Unit = ???
  def inventoryListing:String = ???
  
  def move(dir:Int):Unit = {
    val newRoom:Option[Room] = position.getExit(dir)
    if (newRoom != None) {
      _position = newRoom.get 
      println(position.fullDescription())
    } else println("Leon's poor map building skills prevent you from going this way.\n")
    }
    
  }
