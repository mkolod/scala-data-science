package us.marek.datascience

import breeze.linalg.DenseVector

case class Datum(target: Double, features: DenseVector[Double]) {
  override def toString: String =
    s"Datum(target = $target, features = $features)"
}