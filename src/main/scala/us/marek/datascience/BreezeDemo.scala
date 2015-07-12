package us.marek.datascience

import java.awt.{Color, Paint}

import breeze.linalg.DenseMatrix
import DenseMatrix.{rand => mrand}
import DenseMatrix._
import breeze.optimize.{AdaptiveGradientDescent, LBFGS, DiffFunction}
import breeze.stats.distributions.Rand
import breeze.linalg._
import breeze.plot._

object BreezeDemo extends App {

  // TODO: replace matrices with vectors wherever possible to get rid of matrix-to-vector flattening

  // parameters
  val (numRow, numCol, b, m) = (1000, 1, 2.5, 10.0)

  // create dataset
  val feature = mrand(numRow, numCol, Rand.gaussian)
  val intercept = ones[Double](numRow, numCol)
  val x: DenseMatrix[Double] = horzcat(intercept, feature)
  val y = feature :* m :+ b :+ mrand(feature.rows, feature.cols, Rand.gaussian)

  // inv(X'*X) * X' * y
  val xtxLinRegWts = pinv(x.t * x) * x.t * y

  // SVD
  val svdDec = svd(x)
  val svdLinRegWts = x \ (svdDec.U * svdDec.U.t * y)

  // Optimization
  val linRegDiff = new DiffFunction[DenseVector[Double]] {
    def calculate(wts: DenseVector[Double]): (Double, DenseVector[Double]) = {

      val xty = x * wts.asDenseMatrix.t :- y
      val value = xty.t * xty
      val cost = (value.flatten(View.Prefer))(0)
      val grads = (xty.t * x).flatten(View.Prefer)
      (cost, grads)
    }
  }

  val init = DenseVector.rand(2, Rand.gaussian)

  // Optimization using SGD
  val sgd = new AdaptiveGradientDescent.L2Regularization[DenseVector[Double]](maxIter = 1000, stepSize = 10)
  val sgdResult = sgd.minimize(linRegDiff, init)

  // Optimization using L-BFGS
  val lbfgs = new LBFGS[DenseVector[Double]](maxIter = 1000, m = 5, tolerance = 1e-6)
  val lbfgsResult = lbfgs.minimize(linRegDiff, init)

  val predicted = (x * xtxLinRegWts).flatten(View.Prefer)
  val f = Figure()
  val p = f.subplot(0)
  val feat = feature.flatten(View.Prefer)
  p += scatter(x = y.flatten(View.Prefer), y = predicted, size = (i: Int) => 0.1)
  p.xlabel = "actual"
  p.ylabel = "predicted"
  p.title = "Actual y vs. y predicted using normal equations"

  println(
    s"""
      |Weights for model y = 2.5 + 10 * x + N(0, 1)
      |
      |Using inv(X'X) * X' * y:
      |
      |$xtxLinRegWts
      |
      |Using SVD:
      |
      |$svdLinRegWts
      |
      |Using SGD:
      |
      |${sgdResult.toArray.mkString("\n")}
      |
      |Using L-BFGS:
      |
      |${lbfgsResult.toArray.mkString("\n")}
      |
      |Predicted:
      |$predicted
      |
    """.stripMargin)
}