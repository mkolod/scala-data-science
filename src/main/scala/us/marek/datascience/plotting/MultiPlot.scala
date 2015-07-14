package us.marek.datascience.plotting

import com.quantifind.charts.Highcharts.{
  delete => del, hold, legend => leg, line, redo => red, setPort, startServer, stopServer,
  title => tit, undo => und, unhold, xAxis => xAx, yAxis => yAx
}

import scala.io.StdIn

/**
 * WISP wrapper for multiple line plots on the same chart
 *
 * @author Marek Kolodziej
 *
 * @param x
 * @param y
 * @param xAxis
 * @param yAxis
 * @param title
 * @param legend
 * @param port
 */
case class MultiPlot(x: Seq[Int],
                     y: Seq[Seq[Double]],
                     xAxis: String,
                     yAxis: String,
                     title: String,
                     legend: Seq[String],
                     port: Int = 1234
                      ) {

  def close(): Unit =
    stopServer

  def delete(): Unit =
    del()

  def redo(): Unit =
    red

  def render(): Unit = {

    setPort(port)
    startServer
    line((x, y(0)))
    hold()
    y.tail.foreach(
      series =>
        line((x, series))
    )
    xAx(xAxis)
    yAx(yAxis)
    tit(title)
    leg(legend)
    unhold()
  }

  def renderAndCloseOnAnyKey(): Unit = {

    render()
    StdIn.readLine()
    close()
  }

  def undo(): Unit =
    und()


}