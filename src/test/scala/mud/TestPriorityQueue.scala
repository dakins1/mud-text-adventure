package mud

import org.junit.Test
import org.junit.Assert._

class TestPriorityQueues {

  @Test def SATest: Unit = {
    val nums = Array.fill(100)(math.random)
    val pq = new SAPriorityQueue[Double](_ < _)
    for (n <- nums) pq.enqueue(n)
    for (n <- nums.sorted) {
      assertEquals(n, pq.dequeue(), 0.001)
    }
    for (n <- nums) pq.enqueue(n)
    for (i <- 1 to nums.size) {
      val tmp = pq.peek
      assertEquals(tmp, pq.dequeue(), 0.001)
    }
    val nums1 = Array.fill(100)(math.random)
    for (n <- nums) pq.enqueue(n)
    for (i <- 1 to nums.size/2) {
      val tmp = pq.peek
      assertEquals(tmp, pq.dequeue(), 0.001)
    }
    val nums2 = Array.fill(100)(math.random)
    for (n <- nums2) pq.enqueue(n)
    for (n <- nums) pq.enqueue(n)
    for (i <- 1 to nums.size) {
      val tmp = pq.peek
      assertEquals(tmp, pq.dequeue(), 0.001)
    }
  }

}