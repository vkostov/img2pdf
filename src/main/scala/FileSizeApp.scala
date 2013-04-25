
import akka.actor._
import akka.routing._
import akka.event._
import java.io.File


/**
 * Created by IntelliJ IDEA.
 * User: vkostov
 * Date: 5/6/12
 * Time: 7:40 PM
 */


case class FileToProcess(file: java.io.File)

case class FileSize(size: Long)

case class CalculateSize(file: java.io.File)


class DirWalker(system: ActorSystem) extends Actor {
  def receive = {
    case CalculateSize(file) =>
      if (file.isFile()) {
        system.eventStream.publish(FileSize(file.length()))
      } else {
        var size = 0L
        val children = file.listFiles()

        if (children != null) {
          for (child <- children) {
            if (child.isFile()) {
              size += child.length()
            } else {
              system.eventStream.publish(FileToProcess(child))
            }
          }
        }

        system.eventStream.publish(FileSize(size))
      }
  }

}

class SizeAgregator(system: ActorSystem) extends Actor {
  val start = System.nanoTime()

  var totalSize = 0L
  var filesPendingForProcess = 0L

  def receive = {
    case FileToProcess(file) =>
      filesPendingForProcess += 1

      system.eventStream.publish(CalculateSize(file))

    case FileSize(size) =>
      totalSize += size
      filesPendingForProcess -= 1

    if (filesPendingForProcess == 0) {
      println("Total Size: " + totalSize)
      println("Time taken (s): " + (System.nanoTime() - start) / 1.0e9)

      system.shutdown()
    }
  }


}


object FileSizeApp {

  def main(args: Array[String]): Unit = {

    val system = ActorSystem("FileSizeApp")

    val sizeAggregator = system.actorOf(Props(new SizeAgregator(system)))
    val dirWalkerRouter = system.actorOf(Props(new DirWalker(system)).withRouter(RoundRobinRouter(15)))

    system.eventStream.subscribe(sizeAggregator, classOf[FileSize])
    system.eventStream.subscribe(sizeAggregator, classOf[FileToProcess])

    system.eventStream.subscribe(dirWalkerRouter, classOf[CalculateSize])

    sizeAggregator ! FileToProcess(new File("/Users/vkostov/projects"))

  }


}
