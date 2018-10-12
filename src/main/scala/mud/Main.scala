package mud

import scala.io.StdIn._
import akka.actor.ActorSystem
import akka.actor.Props


object Main extends App {
  val system = ActorSystem("ActorMud")
  val roomManager = system.actorOf(Props(new RoomManager), "RoomManager")
  val playerManager = system.actorOf(Props(new PlayerManager), "PlayerManager")
  
  
  playerManager ! PlayerManager.NewPlayer("p1")
  
  
  
  
  println("\n\n\nHello, and welcome to my mud. \\|^.^|/ \n")
  instructions()
  
  println("You start out at " + Room.rooms("bang").name + "\n")
  println(p1.position.fullDescription() + "\n")
  
  var input = readLine(">")
  while (input != "Dillon is better than me.") {
    p1.processCommand(input)
    input = readLine(">")
  }
   
  def instructions():Unit = {
    println("""-To move around, type in a direction (i.e. north, south, east, etc.).
-To pick up an item, type "grab" and then the name of the item. 
-To drop an item, type "drop" and then the name of the item. 
-To view your inventory, simply type "i".
-To look around your current room, type "look".
-If you want to quit, type "Dillon is better than me." It has to be exactly that.
-Make sure to keep all commands lowercase. Have fun.
""")
  }
  
  def update():Unit = {
    println(p1.position.fullDescription)
  }
  
}