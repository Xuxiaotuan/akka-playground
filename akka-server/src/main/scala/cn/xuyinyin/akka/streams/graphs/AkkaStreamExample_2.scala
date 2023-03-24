package cn.xuyinyin.akka.streams.graphs

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source}
import akka.stream.{ClosedShape, Materializer}

object AkkaStreamExample extends App {

  implicit val system: ActorSystem = ActorSystem("example-system")
  implicit val mat: Materializer   = Materializer(system)

  val source = Source(1 to 10)
  val sink1  = Sink.foreach[Int](x => println(s"sink1 Value is $x"))
  val sink2  = Sink.foreach[Int](x => println(s"sink2 Value is $x"))

  val flow1 = Flow[Int].map(_ * 2)
  val flow2 = Flow[Int].map(_ + 10)

  /**
   *        +---------+
   *        |         |
   * ~> in  |  Bcast  | out(0) ~> flow1 ~> sink1
   * Source |         |------>
   *   ~>   |         | out(1) ~> flow2 ~> sink2
   *        +---------+
   *
   */
  val graph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    val bcast = builder.add(Broadcast[Int](2))
    val flow1Shape = builder.add(flow1)
    val flow2Shape = builder.add(flow2)
    val sink1Shape = builder.add(sink1)
    val sink2Shape = builder.add(sink2)

    source ~> bcast.in
    bcast.out(0) ~> flow1Shape ~> sink1Shape
    bcast.out(1) ~> flow2Shape ~> sink2Shape

    ClosedShape
  })

  graph.run()

}
