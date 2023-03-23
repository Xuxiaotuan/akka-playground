package cn.xuyinyin.akka.streams.graphs.custom

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source

import scala.concurrent.Future

/**
 * Custom processing with GraphStage
 *
 * @author : XuJiaWei
 * @since : 2023-03-23 23:27
 */
object CustomStreamProcessing {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("AkkaStreamDemo")
    implicit val mat: Materializer = Materializer(system)

    // A GraphStage is a proper Graph, just like what GraphDSL.create would return
    val sourceGraph: NumbersSource = new NumbersSource

    // Create a Source from the Graph to access the DSL
    val mysqSource: Source[Int, NotUsed] = Source.fromGraph(sourceGraph)

    // Returns 55
    val result1: Future[Int] = mysqSource.take(10).runFold(0)(_ + _)

    // The source is reusable. This returns 5050
    val result2: Future[Int] = mysqSource.take(100).runFold(0)(_ + _)
  }
}
