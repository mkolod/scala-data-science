package us.marek.datascience

import breeze.linalg.DenseVector

case class OptHistory(
                       cost: Seq[Double],
                       weights: Seq[DenseVector[Double]],
                       grads: Seq[DenseVector[Double]]
                     )