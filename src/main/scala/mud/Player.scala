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
    case "look"  => println(position.fullDescription())
    case "drop"  => dropFromInventory(command.split(" ")(1))
    case "grab" if (command.split(" ").size == 2)  => addToInventory(command.split(" ")(1))
    case "i"     => println(inventoryListing)
    case _       => println("??????????? \n ???? ??? \n??? ?????????? ? \n       ???\n?\n")
  }
  
  
  def dropFromInventory(itemToDrop:String):Unit = {
    val itemOp = items.find(i  => (i.name == itemToDrop))
    if (itemOp != None) {
      _items = _items.filter(s => s != itemOp.get)
      println("You dropped " + itemOp.get.name + " from your inventory.\n")
      position.addItem(itemOp.get)
    }
    else println("You don't have that in your inventory.\n")
  }
  
  
  def addToInventory(itemName:String):Unit = {
    val itemOp = position.items.find(i => (i.name == itemName))
    if (itemOp != None)  {
      _items ::= itemOp.get
      println(itemOp.get.name + " has been added to your inventory.\n")
      position.removeItem(itemOp.get)      
    }
    else println("Leon already picked up this OP weapon.\n")
    
  }
  
  
  def inventoryListing:String = {
    var inventory = ""
    for (i <- _items) inventory += (i.name + " - " + i.desc + "\n")
    inventory
  }
  
  def move(dir:Int):Unit = {
    val newRoom:String = position.getExit(dir)
    if (newRoom != -1) {
      _position = Room.rooms(newRoom) 
      println(position.fullDescription())
    } else println("Leon's poor map building skills prevent you from going this way.\n")
    }
    
  }
