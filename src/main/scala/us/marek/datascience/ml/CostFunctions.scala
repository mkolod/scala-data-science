package us.marek.datascience.ml

import breeze.linalg.DenseVector
import us.marek.datascience.optimization.Types.CostFn
import us.marek.datascience.optimization.VectorizedData
import us.marek.datascience.implicits.DistData

/**
 * Cost functions for various models (e.g. linear regression, logistic regression, neural networks) go here.
 *
 * @author Marek Kolodziej
 */
object CostFunctions {

  val linearRegressionCost = CostFn(
    f =
      (data: DistData[VectorizedData], weights: DenseVector[Double]) => {

        val counts = data.map(_.target.activeSize).reduceLeft(_ + _)

        val unscaledCost = data.aggregate(0.0D)(
          seqOp = {

            case (currCost, elem) => {

              currCost + (elem.features * weights :- elem.target).
                map(i => math.pow(i, 2)).
                reduceLeft(_ + _)
            }
          },
          combOp = {

            case (a, b) => a + b
          }
        )

        unscaledCost / (2 * counts)
      }
  )

}
