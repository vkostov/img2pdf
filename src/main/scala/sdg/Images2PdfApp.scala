package sdg

import akka.actor.{Props, ActorSystem, Actor}
import akka.routing.RoundRobinRouter
import java.io.File
import common._

/**
 * Created by IntelliJ IDEA.
 * User: vkostov
 * Date: 4/24/13
 * Time: 7:58 PM
 */


case class FileToProcess(file: java.io.File)

case class PdfCreated(fileName: String)

case class GeneratePdf(file: java.io.File)

class DirWalker(system: ActorSystem) extends Actor {

  def isEnvelopeDirectory(file: File): Boolean = file.isFile

  def createPdfForEnvelopeDirectory(file: File) = {
    Thread.sleep(1000)    //simulate long running operation
    file.getAbsolutePath
  }

  def receive = {
    case GeneratePdf(file) =>

      if (isEnvelopeDirectory(file) || file.isFile) {
        val pdfFileName = createPdfForEnvelopeDirectory(file)
        system.eventStream.publish(PdfCreated(pdfFileName))
      } else {
        val children = file.listFiles()

        if (children != null) {
          for (child <- children) {
            system.eventStream.publish(FileToProcess(child))
          }
        }
      }
  }
}

class ResultAggregator(system: ActorSystem) extends Actor {
  val start = System.nanoTime()

  var pdfFilesList = List[String]()
  var filesPendingForProcess = 0L

  def receive = {
    case FileToProcess(file) =>
      filesPendingForProcess += 1

      println("Add to Queue: " + filesPendingForProcess)

      system.eventStream.publish(GeneratePdf(file))

    case PdfCreated(fileName) =>
      pdfFilesList = fileName :: pdfFilesList
      filesPendingForProcess -= 1

      println("Remove from Queue: " + filesPendingForProcess)

      if (filesPendingForProcess == 19) {
        println("Number of PDFs created: " + pdfFilesList.size)
        println("Time taken (s): " + (System.nanoTime() - start) / 1.0e9)
        system.shutdown()
      }
  }
}


object Images2PdfApp {

  def main(args: Array[String]): Unit = {
    println("Starting actor system")

    val system = ActorSystem("ImageConversionApp")

    val resultAggregator = system.actorOf(Props(new ResultAggregator(system)))
    val dirWalkerRouter = system.actorOf(Props(new DirWalker(system)).withRouter(RoundRobinRouter(50)))

    system.eventStream.subscribe(resultAggregator, classOf[PdfCreated])
    system.eventStream.subscribe(resultAggregator, classOf[FileToProcess])
    system.eventStream.subscribe(dirWalkerRouter, classOf[GeneratePdf])

    resultAggregator ! FileToProcess(new File("/kidstar/caarc/images/ICD110A/20130424/0177"))

  }

}
