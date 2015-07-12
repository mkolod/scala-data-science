package us.marek.datascience

import breeze.linalg.{DenseMatrix, DenseVector}

case class VectorizedData(target: DenseVector[Double], features: DenseMatrix[Double]) {
  override def toString: String =
    s"""VectorizedData(
       |target = $target,
                          |features = $features
        |)
       """.stripMargin
}