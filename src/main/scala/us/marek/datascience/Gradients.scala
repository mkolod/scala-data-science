package us.marek.datascience

import breeze.linalg.DenseVector

object Gradients {


  def linearRegressionGradients(data: DistData[VectorizedData], weights: DenseVector[Double]):
    DenseVector[Double] = {

    val grads: DenseVector[Double] = data.aggregate(DenseVector.zeros[Double](weights.iterableSize))(
      seqOp = {

        case (partialGrad: DenseVector[Double], datum) =>
          val mul = datum.features * weights
          val sub: DenseVector[Double] = mul :- datum.target
          datum.features.t * sub
      },
      combOp = {
        case (partVec1, partVec2) => partVec1 :+ partVec2
      }
    )
    grads
  }

}
