package us.marek.datascience

import breeze.linalg.DenseVector

object CostFunctions {

  def linearRegressionCost(data: DistData[VectorizedData], weights: DenseVector[Double]): Double = {

    val counts = data.map(i => i.target.activeSize).limitedReduceLeft(_ + _)

    val res = data.aggregate(0.0D)(
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

    res / (2 * counts)
  }

}
