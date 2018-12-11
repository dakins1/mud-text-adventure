package mud

import akka.actor.Actor
import akka.actor.ActorRef

class ActivityManager extends Actor {
  import ActivityManager._

  val schedule = new BinaryHeap[(Int, Any, ActorRef)](_._1 < _._1)
  private var counter = 0

  def receive = {
    case CheckQueue => {
      while (!schedule.isEmpty && schedule.peek._1 <= counter) {
        val tmp = schedule.dequeue()
        tmp._3 ! tmp._2
      }
      counter += 1
//      if (counter % 50 == 0) Server.npcManager ! NPC_Manager.CreateActivity //schedule something for NPCs every 10 seconds
    }

    case ScheduleActivity(delay, message, recipient) =>
      schedule.enqueue(delay + counter + 5, message, recipient)

    case PrintCount =>
      println("current counter " + counter)

    case _ =>
      println("Unhandled message in ActivityManager")
  }
}

object ActivityManager {
  case object Kill
  case object CheckQueue
  case class ScheduleActivity(delay: Int, message: Any, recipient: ActorRef)
  case object PrintCount
}