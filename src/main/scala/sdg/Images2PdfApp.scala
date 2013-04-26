package sdg

import akka.actor.{PoisonPill, Props, ActorSystem, Actor}
import akka.routing.RoundRobinRouter
import java.io.File
import common._
import sdg.Reaper.WatchMe

/**
 * Created by IntelliJ IDEA.
 * User: vkostov
 * Date: 4/24/13
 * Time: 7:58 PM
 */


case class ProcessDirectory(file: java.io.File)

case class PdfCreated(fileName: String)

case class GeneratePdf(file: java.io.File)

class DirWalker(system: ActorSystem) extends Actor {

  def createPdfForEnvelopeDirectory(file: File) = {
    //Thread.sleep(1000) //simulate long running operation
    file.getAbsolutePath
  }

  def receive = {
    case GeneratePdf(file) =>
      val pdfFileName = createPdfForEnvelopeDirectory(file)
      system.eventStream.publish(PdfCreated(pdfFileName))
  }
}

class ResultAggregator(system: ActorSystem) extends Actor {
  val start = System.nanoTime()

  var pdfFilesList = List[String]()
  var filesPendingForProcess = 0L

  def receive = {
    case ProcessDirectory(file) =>
      if (Utils.isEnvelopeDirectory(file.getAbsolutePath) || file.isFile) {
        filesPendingForProcess += 1
        //println("Add to Queue: " + filesPendingForProcess)
        system.eventStream.publish(GeneratePdf(file))
      } else {
        val children = file.listFiles()

        if (children != null) {
          for (child <- children) {
            system.eventStream.publish(ProcessDirectory(child))
          }
        }
      }
    case PdfCreated(fileName) =>
      pdfFilesList = fileName :: pdfFilesList
      filesPendingForProcess -= 1

      //println("Remove from Queue: " + filesPendingForProcess)

      if (filesPendingForProcess == 0) {
        println("Number of PDFs created: " + pdfFilesList.size)
        println("Time taken (s): " + (System.nanoTime() - start) / 1.0e9)
        println(pdfFilesList)
        system.shutdown()
      }
  }
}

object Utils {

  /**
   * Returns true if the directory contains images for a single envelope.
   *
   * The file path uses the following pattern:
   * {basedir}/{file MGR}/{Scan Date}/{iBatch last 4}/{envelope id last 4}/{img file name}
   *
   * Example:
   * /kidstar/caarc/images/ICD110A/20130424/0177/0017/8001.tif
   * /kidstar/caarc/images/ICD110A/20130424/0177/0017/8002.tif
   * /kidstar/caarc/images/ICD110A/20130424/0177/0017/8003.jpg
   * /kidstar/caarc/images/ICD110A/20130424/0177/0017/8004.jpg
   */
  //
  def isEnvelopeDirectory(fileName: String): Boolean = {
    val regexString = """(.*).(\d\d\d\d\d\d\d\d).(\d\d\d\d).(\d\d\d\d)(.*)"""

    //val dirPattern =
    //  new scala.util.matching.Regex(regexString,"basePath, scanDate, batch, envelope")

    fileName.matches(regexString)
  }
}

object Images2PdfApp {

  def main(args: Array[String]): Unit = {
    println("Starting actor system")

    val system = ActorSystem("ImageConversionApp")

    val resultAggregator = system.actorOf(Props(new ResultAggregator(system)))
    val dirWalkerRouter = system.actorOf(Props(new DirWalker(system)).withRouter(RoundRobinRouter(nrOfInstances = 15)))

    system.eventStream.subscribe(resultAggregator, classOf[PdfCreated])
    system.eventStream.subscribe(resultAggregator, classOf[ProcessDirectory])
    system.eventStream.subscribe(dirWalkerRouter, classOf[GeneratePdf])

    resultAggregator ! ProcessDirectory(new File("/kidstar/caarc/images/ICD110A"))
  }
}
