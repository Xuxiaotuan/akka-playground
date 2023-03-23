package cn.xuyinyin.akka.streams.graphs

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source}
import akka.stream.{ClosedShape, Materializer}

/**
 * Constructing Graphs
 *
 * Graphs are built from simple Flows which serve as the linear connections within the graphs as well as junctions
 * which serve as fan-in and fan-out points for Flows.
 *
 * Thanks to the junctions having meaningful types based on their behavior and making them
 * explicit elements these elements should be rather straightforward to use.
 *
 * @author : XuJiaWei
 * @since : 2023-03-23 22:02
 */
object AkkaStreamGraphs {

  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem = ActorSystem("AkkaStreamDemo")
    implicit val mat: Materializer   = Materializer(system)

    RunnableGraph
      .fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
        import GraphDSL.Implicits._
        val in  = Source(1 to 10)
        val out = Sink.foreach[Int](x => println(s"out: Number: $x"))

        val bcast = builder.add(Broadcast[Int](2))
        val merge = builder.add(Merge[Int](2))

        val f1, f2, f3, f4 = Flow[Int].map(_ + 10)
        /**
         * Graph DSL
         *                  +-----------+            +-----------+
         *                  |           |  ~>   f2   |           |
         *   in ~>   f1 ~>  |   bcast   |            |   merge   |  ~> f3 ~> out
         *                  |           |  ~>   f4   |           |
         *                  +-----------+            +-----------+
         */
        in ~> f1 ~> bcast ~> f2 ~> merge ~> f3 ~> out
                    bcast ~> f4 ~> merge
        ClosedShape
      })
      .run()
  }
}
