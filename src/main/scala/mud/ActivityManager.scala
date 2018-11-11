package mud

import akka.actor.Actor
import akka.actor.ActorRef

class ActivityManager extends Actor {
  import ActivityManager._

  val schedule = new SAPriorityQueue[(Int, Any, ActorRef)](_._1 < _._1)
  private var counter = 0

  def receive = {
    case CheckQueue => {
      val command = schedule.peek
      if (command != null && command._1 == counter) {
        val tmp = schedule.dequeue()
        tmp._3 ! tmp._2
      }
      counter += 1
      if (counter % 50 == 0) Server.npcManager ! NPC_Manager.CreateActivity(counter) //schedule every 10 seconds
    }

    case ScheduleActivity(delay, message, sender) =>
      schedule.enqueue(delay, message, sender)

    case _ =>
      println("Unhandled message in ActivityManager")
  }
}

object ActivityManager {
  case object CheckQueue
  case class ScheduleActivity(delay: Int, message: Any, sender: ActorRef)
}