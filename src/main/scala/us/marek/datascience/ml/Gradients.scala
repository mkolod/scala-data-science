package us.marek.datascience.ml

import breeze.linalg.DenseVector
import us.marek.datascience.optimization.Types.GradFn
import us.marek.datascience.optimization.VectorizedData
import us.marek.datascience.implicits.DistData

/**
 * Gradients for various models (e.g. linear regression, logistic regression, neural networks) go here.
 *
 * @author Marek Kolodziej
 */
object Gradients {

  val linearRegressionGradient = GradFn(
    f =
      (data: DistData[VectorizedData], weights: DenseVector[Double]) => {
        data.aggregate(DenseVector.zeros[Double](weights.iterableSize))(
          seqOp = {

            case (partialGrad: DenseVector[Double], datum) =>
              datum.features.t * (datum.features * weights :- datum.target)
          },
          combOp = {

            case (partVec1, partVec2) => partVec1 :+ partVec2
          }
        )
      }
  )

}
