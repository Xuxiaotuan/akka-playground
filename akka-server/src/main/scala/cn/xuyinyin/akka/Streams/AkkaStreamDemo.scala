package cn.xuyinyin.akka.Streams

import akka.stream.{ClosedShape, UniformFanInShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source}
import scala.language.postfixOps
import akka.actor.ActorSystem
import akka.stream.Materializer

/**
 * Akka Streams working with flow sample.
 *
 * https://doc.akka.io/docs/akka/current/stream/stream-flows-and-basics.html
 */

/**
 * @author : XuJiaWei
 * @since : 2023-03-20 23:36
 */
object AkkaStreamDemo {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("AkkaStreamDemo")
    implicit val mat: Materializer   = Materializer(system)

    val in   = Source(1 to 10)
    val out1 = Sink.foreach[Int](x => println(s"out1: Number: $x"))
    // val out2 = Sink.foreach[Int](x => println(s"out2: Number: $x"))

    val processing1 = Flow[Int].map(_ * 2)
    val processing2 = Flow[Int].filter(_ % 2 == 0)

    val graph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val broadcast                          = builder.add(Broadcast[Int](2))
      val merge: UniformFanInShape[Int, Int] = builder.add(Merge[Int](2))

      /**
       * * {{{
       *        +-----------+                 +-----------+
       *        |           |~> processing1   |           |
       *   in ~>| broadcast |                 |   merge   |  ~> out1
       *        |           |~> processing2   |           |
       *        +-----------+                 +-----------+
       * }}}
       */
      in ~> broadcast ~> processing1 ~> merge ~> out1
      broadcast ~> processing2 ~> merge

      ClosedShape
    })

    graph.run()
  }
}
