package us.marek.datascience

import breeze.linalg.{DenseMatrix, DenseVector}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkContext, SparkConf}
import us.marek.datascience.CostFunctions._
import us.marek.datascience.Gradients._
import Optimizer.optimize

import scala.util.Random


/**
 * @author Marek Kolodziej
 */
object SGDDemo extends App {

  @transient val log = Logger.getLogger(SGDDemo.getClass)


  val conf = new SparkConf().setAppName("sgd").setMaster("local[1]")
  val sc = new SparkContext(conf)

  val optSeed = 42L
  val rand = new Random(42L)
  val numEl = 1000
  val (a, m) = (3.0D, 10.0D)
  val feature = Array.fill(numEl)(rand.nextDouble())
  val targets = feature.map(i => a + m * i + rand.nextDouble() / 100)
  val data =
    targets.
      zip(feature).
      map { case (y, x) => Datum(y, DenseVector[Double](1.0D, x)) }

  val allIt = 100
  val allAlpha = 0.1
  val allMomentum = 0.9
  val allFrac = 0.01
  val numGroups = 10

  // TODO: refactor
  val groupedData: Seq[(Int, Array[Datum])] =
    data.
      zipWithIndex.
      groupBy(d => d._2 % numGroups).
      map(i => (i._1, i._2.map(_._1))).
      toSeq

  def transform(in: DistData[(Int, Array[Datum])]): DistData[VectorizedData] = {
    in.
      map {
      case (_, arr) =>
        // TODO: fix this since it's unsafe
        val numFeat = arr.headOption.get.features.iterableSize
        val init = (DenseVector.zeros[Double](0), DenseMatrix.zeros[Double](0, numFeat))
        val folded = arr.foldLeft(init)(
          (acc, elem) => {
            val vecCat = DenseVector.vertcat(acc._1, DenseVector(elem.target))
            val featMat = elem.features.toDenseMatrix
            val matCat = DenseMatrix.vertcat(acc._2, featMat)
            (vecCat, matCat)
          }
        )
        VectorizedData(target = folded._1, features = folded._2)
    }
  }

  val localData: DistData[VectorizedData] = transform(groupedData)
  val rdd: DistData[VectorizedData] = transform(sc.parallelize(groupedData))

//  localData.zipWithIndex.foreach {
//    case (elem: VectorizedData, idx) =>
//      println(s"\n\nGroup # $idx\n")
//      println(s"Target:\n")
//      println(elem.target)
//      println(s"\nFeatures:\n")
//      println(elem.features)
//      println("\n")
//  }

  val vectLocalOptSGDWithMomentum = optimize(
    data = localData,
    iter = allIt,
    seed = optSeed,
    initAlpha = allAlpha,
    momentum = 0.9,
    gradFn = linearRegressionGradients,
    costFn = linearRegressionCost,
    miniBatchFraction = allFrac,
    adaGrad = false
  )

  val vectLocalOptAdaWithMomentum = optimize(
    data = rdd,
    iter = allIt,
    seed = optSeed,
    initAlpha = allAlpha,
    momentum = 0.9,
    gradFn = linearRegressionGradients,
    costFn = linearRegressionCost,
    miniBatchFraction = allFrac,
    adaGrad = true
  )

  // TODO: refactor


  import com.quantifind.charts.Highcharts._

  startServer

  // TODO: put this code in a wrapper
  val x2 = 1 to vectLocalOptSGDWithMomentum.cost.size
  line((x2, vectLocalOptSGDWithMomentum.cost))
  hold()
  line((x2, vectLocalOptAdaWithMomentum.cost))
  legend(Seq("Scala collections + SGD", "Spark RDD + Adagrad"))
  xAxis("Iteration")
  yAxis("Cost")
  title("Comparison of SGD with Scala collections and Adagrad with Spark ")
  unhold()

  scala.io.StdIn.readLine("Press any key to continue...")
  stopServer
  sc.stop()

  log.info(s"Vectorized SGD: momentum = 0.9, alpha = 0.5, miniBatchFrac = $allFrac, iter = $allIt:\n${vectLocalOptSGDWithMomentum.weights.last}")
  log.info(s"Vectorized Adagrad: momentum = 0.9, alpha = 0.5, miniBatchFrac = $allFrac, iter = $allIt:\n${vectLocalOptAdaWithMomentum.weights.last}")

}
