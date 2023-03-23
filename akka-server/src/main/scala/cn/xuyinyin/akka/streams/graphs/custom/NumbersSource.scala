package cn.xuyinyin.akka.streams.graphs.custom

import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}
import akka.stream.{Attributes, Outlet, SourceShape}

/**
 * @author : XuJiaWei
 * @since : 2023-03-23 23:31
 */
class NumbersSource extends GraphStage[SourceShape[Int]] {

  // Define the (sole) output port of this stage
  val out: Outlet[Int] = Outlet("NumbersSource")

  // Define the shape of this stage, which is SourceShape with the port we defined above
  override def shape: SourceShape[Int] = SourceShape(out)

  // This is where the actual (possibly stateful) logic will live
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      // All state MUST be inside the GraphStageLogic,
      // never inside the enclosing GraphStage.
      // This state is safe to access and modify from all the
      // callbacks that are provided by GraphStageLogic and the
      // registered handlers.
      private var counter = 1

      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          push(out, counter)
          counter += 1
        }
      })
    }

}
