package us.marek.datascience

import breeze.linalg.{DenseMatrix, DenseVector}
import org.apache.spark.{SparkConf, SparkContext}
import CostFunctions.linearRegressionCost
import Gradients.linearRegressionGradients

import org.apache.log4j.{Level, Logger}

import scala.util.Random

/**
 * @author Marek Kolodziej
 */
object Optimizer {

  @transient val log = Logger.getLogger(Optimizer.getClass)

  def weightUpdate(data: DistData[VectorizedData],
                                    history: OptHistory,
                                    gradFn: (DistData[VectorizedData], DenseVector[Double]) => DenseVector[Double],
                                    costFn: (DistData[VectorizedData], DenseVector[Double]) => Double,
                                    initAlpha: Double,
                                    momentum: Double,
                                    miniBatchFraction: Double,
                                    miniBatchIterNum: Int,
                                    adaGrad: Boolean,
                                    seed: Long = 42L): OptHistory = {


    val collCount = data.size
    val exampleCount = data.map(i => i.target.activeSize).limitedReduceLeft(_ + _)
    val exampleStepSize = miniBatchFraction * exampleCount
    val histLen = history.cost.size
    val currSeed = seed + miniBatchIterNum

    val regularSampling = collCount >= math.ceil(1.0/miniBatchFraction)

    // TODO: move this logic to sampling
    val sample: DistData[VectorizedData] =
      if (regularSampling) {

        log.info(s"\n\nRegular sampling")
        val smpl = data.sample(withReplacement = false, fraction = miniBatchFraction, seed = currSeed)
        log.info(s"sMinibatch fraction = $miniBatchFraction")
        log.info(s"Number of elements in sample: ${smpl.map(i => i.target.activeSize).limitedReduceLeft(_ + _)}")
        log.info(s"Number of elements in dataset: ${exampleCount}\n\n")
        log.info(smpl)
        smpl

      } else {

        log.info(s"\n\nSpecial sampling\n\n")
        val smpl = data.map {

          case i: VectorizedData =>

            val size = i.target.activeSize
            val rounded = math.round(miniBatchFraction * size).toInt

            val rowIndices = Sampling.sample(
              coll = (0 until size),
              sampleSize = rounded,
              withReplacement = false,
              seed = currSeed
            )

            VectorizedData(
              target = i.target(rowIndices).toDenseVector,
              features = i.features(rowIndices, ::).toDenseMatrix
            )
        }
        smpl
      }

    val sampleSize = sample.map(_.target.activeSize).limitedReduceLeft(_ + _)

    val weights = history.weights.last
    val newCost = costFn(sample, weights)
    val gradients = gradFn(sample, weights)
    val prevDeltaW = history.weights(histLen - 1) :- history.weights(histLen - 2)
    val eta = initAlpha / math.sqrt(sampleSize * miniBatchIterNum)
    val mom: DenseVector[Double] = prevDeltaW :* momentum
    val newWtsNoMom: DenseVector[Double] = weights :- (gradients :* eta)
    val gradWithMom = (gradients :* eta) :+ mom
    val newWtsWithMom = newWtsNoMom :+ mom
    val adaGradDiag: DenseVector[Double] =
      history.grads.foldLeft(DenseVector.zeros[Double](weights.iterableSize))((acc: DenseVector[Double], item: DenseVector[Double]) => {
        val temp: Array[Double] = acc.toArray.zip(item.toArray).map(i  => i._1 + math.pow(i._2, 2))
        new DenseVector[Double](temp)
      })
    val scaledByDiag = new DenseVector[Double](gradients.toArray.zip(adaGradDiag.toArray).map(
      i =>
        initAlpha * i._1 / math.sqrt(i._2)
    ))
    val adaGradWts = (weights :- scaledByDiag) :+ mom
    val newWeights = if (adaGrad) adaGradWts else newWtsWithMom
    val gradHist = if (adaGrad) scaledByDiag else gradWithMom
    OptHistory(cost = history.cost :+ newCost, weights = history.weights :+ newWeights, grads = history.grads :+ gradHist)
  }


  def initWeights(numEl: Int, seed: Long): DenseVector[Double] = {
    val rand = new Random(seed)
    new DenseVector[Double](Array.fill(numEl)(rand.nextDouble()))
  }
  
  def optimize(data: DistData[VectorizedData],
                         iter: Int,
                         seed: Long = 42L,
                         initAlpha: Double = 0.1,
                         momentum: Double = 0.0,
                         gradFn:  (DistData[VectorizedData], DenseVector[Double]) => DenseVector[Double],
                         costFn: (DistData[VectorizedData], DenseVector[Double]) => Double,
                         miniBatchFraction: Double,
                         adaGrad: Boolean
              ): OptHistory = {

    val count = data.size
    val dataSize = data.headOption match {
      case Some(x) => x.features.cols
      case None => 0
    }
    val initWts = initWeights(dataSize, seed)
    val initGrads = initWeights(dataSize, seed + 1)
    // TODO: this cost is for the whole dataset, but we want a value for just one mini-batch
    val initCost = linearRegressionCost(data, initWts)
    val initHistory = OptHistory(cost = Seq(initCost, initCost), weights = Seq(initWts, initWts), grads = Seq(initGrads, initGrads))

    (1 to iter).foldLeft (initHistory) {

      case (history, it) =>
        if (it == iter) history
        else weightUpdate(data, history, gradFn, costFn, initAlpha, momentum, miniBatchFraction, it, adaGrad, seed)
    }
  }
}