package us.marek.datascience

import com.quantifind.charts.Highcharts._

object WispDemo extends App {

  startServer

  val x = List(1, 2, 3, 4, 5)
  val y = List(4, 1, 3, 2, 6)

  areaspline(x.zip(y))
  pie(Seq(4, 4, 5, 9))

  regression((0 until 100).map(x => -x + scala.util.Random.nextInt(25)))

  histogram(Seq(1, 2, 2, 3, 3, 4, 4, 4, 4, 4, 5, 5, 5, 6, 7), 7)

  bar((0 until 20).map(_ % 8))
  hold
  bar((0 until 20).map(_ % 4))
  stack()
  title("Stacked Bars")
  xAxis("Quantity")
  yAxis("Price")
  legend(List("Blue", "Black"))


  scala.io.StdIn.readLine("Press any key to continue...")

  stopServer

}
