package mud

import scala.io.StdIn._
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Actor
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import java.io.PrintStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket

object Main extends App {
  val system = ActorSystem("ActorMud")
  val roomManager = system.actorOf(Props(new RoomManager), "RoomManager")
  val playerManager = system.actorOf(Props(new PlayerManager), "PlayerManager")

  system.scheduler.schedule(1.seconds, 0.1.seconds, playerManager, PlayerManager.CheckAllInput)

  val ss = new ServerSocket(4040)

  while (true) {
    val sock = ss.accept()
    Future {
      val ps = new PrintStream(sock.getOutputStream)
      val br = new BufferedReader(new InputStreamReader(sock.getInputStream))
      ps.println("\nWhat is your name?\n")
      val name = br.readLine()
      playerManager ! PlayerManager.NewPlayer(name, sock, ps, br)
    }

  }
  
}