package us.marek.datascience.optimization

import breeze.linalg.{DenseMatrix, DenseVector}

/**
 * Group a bunch of examples into a feature matrix and a target vector,
 * instead of processing a feature vector and a target value at a time.
 * This will allow for vectorizing the linear algebra. When Breeze's
 * BLAS support is available (see https://github.com/fommil/netlib-java),
 * Breeze will execute linear algebra operations natively, benefiting from
 * lack of garbage collection, vectorization via SSE, etc.
 *
 * @author Marek Kolodziej
 *
 * @param target
 * @param features
 */
case class VectorizedData(target: DenseVector[Double], features: DenseMatrix[Double]) {
  override def toString: String =
    s"""VectorizedData(
       |target = $target,
       |features = $features
       |)
       """.stripMargin
}