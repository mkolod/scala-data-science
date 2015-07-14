package us.marek.datascience.optimization

import breeze.linalg.DenseVector

/**
 * Wrapper for a single example (target and features)
 *
 * @author Marek Kolodziej
 *
 * @param target
 * @param features
 */
case class Datum(target: Double, features: DenseVector[Double]) {
  override def toString: String =
    s"Datum(target = $target, features = $features)"
}