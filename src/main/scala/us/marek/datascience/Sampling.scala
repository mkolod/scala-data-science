package us.marek.datascience

import scala.util.Random

object Sampling {

  def sample[A](coll: Traversable[A], sampleSize: Int, withReplacement: Boolean, seed: Long = System.nanoTime): IndexedSeq[A] = {

    val rand = new Random(seed)

    @annotation.tailrec
    def collect(seq: IndexedSeq[A], size: Int, acc: List[A]) : List[A] = {
      if (size == 0) acc
      else {
        val index = rand.nextInt(seq.size)
        collect(seq.updated(index, seq(0)).tail, size - 1, seq(index) :: acc)
      }
    }
    collect(coll.toIndexedSeq, sampleSize, Nil).toIndexedSeq
  }

}