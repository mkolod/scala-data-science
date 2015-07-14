package us.marek.datascience.optimization

import breeze.linalg.DenseVector
import org.apache.log4j.Logger
import us.marek.datascience.demo.SGDAdagradDemo
import us.marek.datascience.optimization.Types.{WeightUpdate, WeightInit, CostFn, GradFn}
import Sampling.sampleMiniBatch
import us.marek.datascience.typeclasses.DistData

/**
 * The optimizization algorithms (SGD, Adagrad, L-BFGS, etc.) go here.
 *
 * @author Marek Kolodziej
 */
object OptimAlgos {

  @transient val log = Logger.getLogger(SGDAdagradDemo.getClass)

  // helper class to make the SGD and Adagrad code more DRY, since this is repetitive stuff
  private case class OptInfo(
                              private val data: DistData[VectorizedData],
                              private val miniBatchFraction: Double,
                              private val currSeed: Long,
                              private val history: OptHistory,
                              private val costFn: CostFn,
                              private val gradFn: GradFn
                              ) {

    val weights = history.weights.last
    private val histLen = history.cost.size
    lazy val sample = sampleMiniBatch(data, miniBatchFraction, currSeed)
    lazy val sampleSize = sample.map(_.target.activeSize).reduceLeft(_ + _)
    lazy val newCost = costFn(sample, weights)
    lazy val gradients = gradFn(sample, weights)
    lazy val prevDeltaW = history.weights(histLen - 1) :- history.weights(histLen - 2)
  }

  /* stochastic gradient descent
     see http://leon.bottou.org/publications/pdf/online-1998.pdf
   */
  val sgd = WeightUpdate(
    f = (data: DistData[VectorizedData],
         history: OptHistory,
         gradFn: GradFn,
         costFn: CostFn,
         initAlpha: Double,
         momentum: Double,
         miniBatchFraction: Double,
         miniBatchIterNum: Int,
         seed: Long) => {

      val opt = OptInfo(data, miniBatchFraction, seed + miniBatchIterNum, history, costFn, gradFn)
      val eta = initAlpha / math.sqrt(opt.sampleSize * miniBatchIterNum)
      val mom: DenseVector[Double] = opt.prevDeltaW :* momentum
      val newWtsNoMom: DenseVector[Double] = opt.weights :- (opt.gradients :* eta)
      val gradWithMom = (opt.gradients :* eta) :+ mom
      val newWtsWithMom = newWtsNoMom :+ mom
      OptHistory(
        cost = history.cost :+ opt.newCost,
        weights = history.weights :+ newWtsWithMom,
        grads = history.grads :+ gradWithMom
      )
    }
  )

  /* Adagrad
     see http://www.jmlr.org/papers/volume12/duchi11a/duchi11a.pdf
   */
  val adaGrad = WeightUpdate(
    f = (data: DistData[VectorizedData],
         history: OptHistory,
         gradFn: GradFn,
         costFn: CostFn,
         initAlpha: Double,
         momentum: Double,
         miniBatchFraction: Double,
         miniBatchIterNum: Int,
         seed: Long) => {

      val opt = OptInfo(data, miniBatchFraction, seed + miniBatchIterNum, history, costFn, gradFn)
      val mom: DenseVector[Double] = opt.prevDeltaW :* momentum
      val adaGradDiag: DenseVector[Double] =
        history.grads.foldLeft(
          DenseVector.zeros[Double](opt.weights.iterableSize)
        )(
            (acc: DenseVector[Double], item: DenseVector[Double]) => {
              val temp: Array[Double] = acc.toArray.zip(item.toArray).map(i => i._1 + math.pow(i._2, 2))
              new DenseVector[Double](temp)
            })
      val scaledByDiag = new DenseVector[Double](
        opt.gradients.toArray.zip(adaGradDiag.toArray).map(
          i =>
            initAlpha * i._1 / math.sqrt(i._2)
        )
      )
      val adaGradWts = (opt.weights :- scaledByDiag) :+ mom
      OptHistory(
        cost = history.cost :+ opt.newCost,
        weights = history.weights :+ adaGradWts,
        grads = history.grads :+ scaledByDiag
      )
    }
  )
}
