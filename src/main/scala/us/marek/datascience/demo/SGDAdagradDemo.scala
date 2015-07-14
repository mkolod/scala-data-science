package us.marek.datascience.demo

import breeze.linalg.DenseVector
import org.apache.log4j.Logger
import org.apache.spark.{SparkConf, SparkContext}
import us.marek.datascience.ml.{CostFunctions, Gradients}
import CostFunctions._
import Gradients._
import us.marek.datascience.optimization.OptimAlgos._
import us.marek.datascience.optimization.Optimizer._
import us.marek.datascience.optimization.Types.WeightUpdate
import us.marek.datascience.util.DataConversions
import DataConversions.toVectorizedData
import us.marek.datascience.optimization.WeightInitializer._
import us.marek.datascience.optimization.{Datum, VectorizedData}
import us.marek.datascience.plotting.MultiPlot
import us.marek.datascience.typeclasses.DistData

import scala.util.Random


/**
 *
 * This example shows how to generate sample data to run linear regression using
 * both Scala collections and Spark. The optimizers used here are stochastic
 * gradient descent (SGD) and Adagrad.
 *
 * @author Marek Kolodziej
 */
object SGDAdagradDemo extends App {

  @transient val log = Logger.getLogger(SGDAdagradDemo.getClass)

  // Set up Spark
  val conf = new SparkConf().setAppName("sgd_and_adagrad_demo").setMaster("local[1]")
  val sc = new SparkContext(conf)

  // Generate data for linear regression: y = 3.0 + 10.0 * x + error
  val rand = new Random(42L)
  val numExamples = 1000
  val (intercept, slope) = (3.0D, 10.0D)
  val feature = Seq.fill(numExamples)(rand.nextDouble())
  val targets = feature.map(i => intercept + slope * i + rand.nextDouble() / 100)
  val data =
    targets.
      zip(feature).
      map {
        // merge target and feature, add intercept to feature vector
        case (y, x) => Datum(y, DenseVector[Double](1.0D, x))
      }

  val allFrac = 0.1
  val numGroups = 200
  val allIt = 200

  val localData: DistData[VectorizedData] = toVectorizedData(data = data, numExamplesPerGroup = 10)
  val rdd: DistData[VectorizedData] = toVectorizedData(data = sc.parallelize(data), numExamplesPerGroup = 10)

  /* partially applied function - we'll specify the weight update algorithm and dataset
     for the two cases separately, while reusing common stuff
   */
  val commonParams = optimize(
    iter = allIt,
    seed = 123L,
    initAlpha = 0.1,
    momentum = 0.9,
    gradFn = linearRegressionGradient,
    costFn = linearRegressionCost,
    _: WeightUpdate,
    miniBatchFraction = allFrac,
    weightInitializer = gaussianInit,
    _: DistData[VectorizedData]
  )

  val vectLocalOptSGDWithMomentum =
    commonParams(sgd, localData)

  val vectSparkOptAdaWithMomentum =
    commonParams(adaGrad, rdd)

  // TODO: add descriptions (expected coefficients and ones estimated by SGD and Adagrad)


  val dropEl = 1
  val plot = MultiPlot(
    x = dropEl to allIt, //allIt/2
    y = Seq(
      vectLocalOptSGDWithMomentum.cost.drop(dropEl),
      vectSparkOptAdaWithMomentum.cost.drop(dropEl)
    ),
    xAxis = "Iteration",
    yAxis = "Cost",
    title = s"Expected: y ≈ $intercept + $slope * x",
    legend = Seq(
      f"Scala collections + SGD: y ≈ ${vectLocalOptSGDWithMomentum.weights.last(0)}%.1f + ${vectLocalOptSGDWithMomentum.weights.last(1)}%.1f * x",
      f"Spark RDD + Adagrad: y ≈ ${vectSparkOptAdaWithMomentum.weights.last(0)}%.1f + ${vectSparkOptAdaWithMomentum.weights.last(1)}%.1f * x"
    ),
    port = 1234
  )

  plot.renderAndCloseOnAnyKey()

  sc.stop()



}
