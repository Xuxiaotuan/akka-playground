package cn.xuyinyin.akka

import akka.actor.testkit.typed.scaladsl.{LogCapturing, TestProbe}
import akka.actor.typed.{ActorRef, ActorSystem}
import com.typesafe.config.ConfigFactory
import org.scalatest.Suite
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object TestImplicit {

  implicit class TestProbeWrapper[M](probe: TestProbe[M]) {
    def to[R](func: TestProbe[M] => R): R = func(probe)
    def from(func: ActorRef[M] => Unit): TestProbe[M] = {
      func(probe.ref)
      probe
    }
  }

  def testProbe[M](func: TestProbe[M] => Any)(implicit system: ActorSystem[Nothing]): TestProbe[M] = {
    val probe = TestProbe[M]()
    func(probe)
    probe
  }

  implicit class WrappedFuture[T](f: Future[T]) {
    def receive(timeout: Duration = Duration.Inf): T = Await.result(f, Duration.Inf)
  }

}


class TimeDotter {
  private var preDot = System.currentTimeMillis()
  def dot(): Unit = {
    val cur = System.currentTimeMillis()
    println(s"Cost: ${cur - preDot} millis")
    preDot = cur
  }
}
object TimeDotter {
  def dot(): TimeDotter = new TimeDotter()
  def printCost(func: => Unit): Unit = {
    val dotter = dot()
    func
    dotter.dot()
  }
}



trait STAkkaSpec extends AnyWordSpecLike with LogCapturing {

  /*  implicit class TestProbeWrapper[M](probe: TestProbe[M]) {
      def to[R](func: TestProbe[M] => R): R = func(probe)
      def from(func: ActorRef[M] => Unit): TestProbe[M] = {
        func(probe.ref)
        probe
      }
    }*/

  val logListenerConfig = ConfigFactory.parseString("""akka.loggers = ["akka.testkit.TestEventListener"]""")

  def testProbe[M](func: TestProbe[M] => Any)(implicit system: ActorSystem[Nothing]): TestProbe[M] = {
    val probe = TestProbe[M]()
    func(probe)
    probe
  }

  def testProbeRef[M](func: ActorRef[M] => Any)(implicit system: ActorSystem[Nothing]): TestProbe[M] = {
    val probe = TestProbe[M]()
    func(probe.ref)
    probe
  }

}

class RunSpec(spec: Suite, testName: String) extends App {
    spec.execute(testName = testName)
}

// object ActorStyleSpec1 extends RunSpec(new ActorStyleSpec,"Counter should functional style")
