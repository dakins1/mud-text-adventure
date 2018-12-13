package mud

import collection.mutable

class BSTMap[K, V](lt: (K, K) => Boolean) extends mutable.Map[K, V] {
  import BSTMap._

  private var root: Node[K, V] = null

  def get(key: K): Option[V] = {
    var rover = root
    while (rover != null) {
      if (lt(rover.key, key)) rover = rover.right
      else if (lt(key, rover.key)) rover = rover.left
      else return Some(rover.value)
    }
    None
  }


  def iterator = new Iterator[(K, V)] {
    var stack: List[Node[K, V]] = Nil
    pushAllLeft(root)
    def hasNext: Boolean = stack.nonEmpty 
    def next(): (K, V) = {
      val n = stack.head
      stack = stack.tail
      pushAllLeft(n.right)
      (n.key, n.value)
    }
    def pushAllLeft(n: Node[K, V]): Unit = {
      if(n != null) {
        stack ::= n
        pushAllLeft(n.left)
      }
    }
  }

  def toBSTMap(arr:Array[(K,V)]): BSTMap.this.type = {
    for (p <- arr) +=(p._1 -> p._2)
    this
  }


  def +=(kv: (K, V)): BSTMap.this.type = {
    if (root == null) root = new Node[K, V](kv._1, kv._2, null, null)
    else {
      var rover = root
      var trailer = root
      while (rover != null) {
        trailer = rover
        if (lt(rover.key, kv._1)) rover = rover.right
        else if (lt(kv._1, rover.key)) rover = rover.left
        else {
          rover.value = kv._2
          return this
        }
      }
      if (lt(kv._1, trailer.key)) trailer.left = new Node[K, V](kv._1, kv._2, null, null)
      else trailer.right = new Node[K, V](kv._1, kv._2, null, null)
    }
    this
  }
 
  def -=(k:K): BSTMap.this.type = {
    this
  }
}

object BSTMap {
  private class Node[K, V](val key: K, var value: V, var left: Node[K, V], var right: Node[K, V]) {
    override def toString() = s"$key, $value"
  }
  
}