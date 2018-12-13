package mud

class SLLPriorityQueue[A](highP: (A, A) => Boolean) {
  private class Node(val data: A, var prev: Node, var next: Node)
  private var sz = 0
  private var sentinel = new Node(null.asInstanceOf[A], null, null)
  sentinel.next = sentinel
  sentinel.prev = sentinel

  def enqueue(a: A): Unit = {
    val n = new Node(a, null, null)
    if (sentinel.next == null) {
      sentinel.next = new Node(a, sentinel, sentinel)
      sentinel.prev = sentinel.next
    }
    else {
      var rover = sentinel.next
      while (rover != null && highP(rover.data, a)) {
        rover = rover.next
      }
      n.next = rover
      n.prev = rover.prev
      rover.prev.next = n
      rover.prev = n
    }
    sz += 1
  }

  def dequeue():A = {
    val ret = sentinel.next
    sentinel.next = sentinel.next.next
    sentinel.next.prev = sentinel
    sz -= 1
    ret.data
  }
  
  def peek():A = {
    sentinel.next.data
  }
  def isEmpty():Boolean = sz == 0
  
}