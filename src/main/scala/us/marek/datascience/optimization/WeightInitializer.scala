package us.marek.datascience.optimization

import breeze.linalg.DenseVector
import us.marek.datascience.optimization.Types.WeightInit

import scala.util.Random

/**
 * @author Marek Kolodziej
 */
object WeightInitializer {

  val gaussianInit = WeightInit(
    f = (numEl: Int, seed: Long) => {
      val rand = new Random(seed)
      new DenseVector[Double](Array.fill(numEl)(rand.nextGaussian()))
    }
  )
}
