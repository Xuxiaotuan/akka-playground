package cn.xuyinyin.akka.streams.graphs.custom

import akka.actor.ActorSystem
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler, StageLogging}
import akka.stream._
import akka.stream.scaladsl.BroadcastHub.sink
import akka.stream.scaladsl.Source

import java.util.concurrent.ThreadLocalRandom

/**
 * @author : XuJiaWei
 * @since : 2023-03-24 11:16
 */
object StreamGraphs {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("AkkaStreamDemo")
    implicit val mat: Materializer   = Materializer(system)

    val resultFuture =
      Source(1 to 5)
        .via(new Filter(_ % 2 == 0))
        .via(new Duplicator())
        .via(new Map(_ / 2))
        .wireTap(println(_))
        .runWith(sink)

  }
}

/**
 * Map is a typical example of a one-to-one transformation of a stream
 * where demand is passed along upstream elements passed on downstream.
 *
 * @param f
 * @tparam A
 * @tparam B
 */
class Map[A, B](f: A => B) extends GraphStage[FlowShape[A, B]] {

  val in: Inlet[A]   = Inlet[A]("Map.in")
  val out: Outlet[B] = Outlet[B]("Map.out")

  override def shape: FlowShape[A, B] = FlowShape.of(in, out)

  /**
   *  Map
   *
   *  |---------------------------------|
   *  | onPush()             push(out,b)|
   *  |------------>------->------------|
   *  |                                 |
   *  |                                 |
   *  |               demand            |
   *  |------------<-------<------------|
   *  |pull(in)                 onPull()|
   *  |---------------------------------|
   *
   * @param attr
   * @return
   */
  override def createLogic(attr: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      setHandler(
        in,
        new InHandler {
          override def onPush(): Unit = push(out, f(grab(in)))
        })

      setHandler(
        out,
        new OutHandler {
          override def onPull(): Unit = pull(in)
        })
    }
}

/**
 *  Filter
 *
 *  |---------------------------------|
 *  | onPush()             push(out,b)|
 *  |------------>------->------------|
 *  |            | if(p(elem)         |
 *  |if(!p(elem))|                    |
 *  |            | demand             |
 *  |------------<-------<------------|
 *  |pull(in)                 onPull()|
 *  |---------------------------------|
 * @param p
 * @tparam A
 */
class Filter[A](p: A => Boolean) extends GraphStage[FlowShape[A, A]] {

  val in: Inlet[A]           = Inlet[A]("Filter.in")
  val out: Outlet[A]         = Outlet[A]("Filter.out")
  val shape: FlowShape[A, A] = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      setHandler(
        in,
        new InHandler {
          override def onPush(): Unit = {
            val elem = grab(in)
            if (p(elem)) push(out, elem)
            else pull(in)
          }
        })
      setHandler(
        out,
        new OutHandler {
          override def onPull(): Unit = {
            pull(in)
          }
        })
    }
}

class Duplicator[A] extends GraphStage[FlowShape[A, A]] {

  val in  = Inlet[A]("Duplicator.in")
  val out = Outlet[A]("Duplicator.out")

  val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      // Again: note that all mutable state
      // MUST be inside the GraphStageLogic
      var lastElem: Option[A] = None

      setHandler(
        in,
        new InHandler {
          override def onPush(): Unit = {
            val elem = grab(in)
            lastElem = Some(elem)
            push(out, elem)
          }

          override def onUpstreamFinish(): Unit = {
            if (lastElem.isDefined) emit(out, lastElem.get)
            complete(out)
          }

        })
      setHandler(
        out,
        new OutHandler {
          override def onPull(): Unit = {
            if (lastElem.isDefined) {
              push(out, lastElem.get)
              lastElem = None
            } else {
              pull(in)
            }
          }
        })
    }
}

final class RandomLettersSource extends GraphStage[SourceShape[String]] {
  val out                                 = Outlet[String]("RandomLettersSource.out")
  override val shape: SourceShape[String] = SourceShape(out)

  override def createLogic(inheritedAttributes: Attributes) =
    new GraphStageLogic(shape) with StageLogging {
      setHandler(
        out,
        new OutHandler {
          override def onPull(): Unit = {
            val c = nextChar() // ASCII lower case letters

            // `log` is obtained from materializer automatically (via StageLogging)
            log.debug("Randomly generated: [{}]", c)

            push(out, c.toString)
          }
        })
    }

  def nextChar(): Char =
    ThreadLocalRandom.current().nextInt('a', 'z'.toInt + 1).toChar
}
