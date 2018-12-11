package mud

import scala.reflect.ClassTag

class BinaryHeap[A:ClassTag] (highP: (A,A) => Boolean) {
  private var heap = new Array[A](12)
  private var back = 1
  def dequeue(): A = {
    val ret = heap(1)
    var stone = 1
    val sinker = heap(back - 1)
    back -= 1
    var flag = true
    while(flag && stone*2 < back) {
      var higherPChild = stone * 2
      if(stone * 2 + 1 < back && highP(heap(stone*2 + 1), heap(higherPChild))) {
        higherPChild += 1
      }
      if(highP(heap(higherPChild), sinker)) {
        heap(stone) = heap(higherPChild)
        stone = higherPChild
      } else {
        flag = false
      }
    }
    heap(stone) = sinker
    ret
  }
  def enqueue(a: A): Unit = {
    if(back >= heap.length) {
      val tmp = new Array[A](heap.length*2)
      for(i <- 1 until heap.length) {
        tmp(i) = heap(i)
      }
      heap = tmp
    }
    var bubble = back
    while(bubble > 1 && highP(a, heap(bubble/2))){
      heap(bubble) = heap(bubble/2)
      bubble /= 2
    }
    heap(bubble) = a
    back += 1
  }
  def isEmpty: Boolean = back == 1
  def peek: A = heap(1)
}