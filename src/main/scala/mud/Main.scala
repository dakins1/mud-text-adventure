package mud

import scala.io.StdIn._
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Actor
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


object Main extends App {
  val system = ActorSystem("ActorMud")
  val roomManager = system.actorOf(Props(new RoomManager), "RoomManager")
  val playerManager = system.actorOf(Props(new PlayerManager), "PlayerManager")  
  
  playerManager ! PlayerManager.NewPlayer("p1")

  system.scheduler.schedule(1.seconds, 0.1.seconds, playerManager, PlayerManager.CheckAllInput)


  
}