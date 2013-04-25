import akka.actor.{ActorSystem, Actor}

/**
 * Created by IntelliJ IDEA.
 * User: vkostov
 * Date: 4/24/13
 * Time: 7:58 PM
 */



class Supervisor(system: ActorSystem) extends Actor {
  def receive = {

  }
}

class Worker(system: ActorSystem) extends Actor {
  def receive = {

  }
}


object Images2PdfApp {

  def main(args: Array[String]): Unit = {
    println("Starting actor system")




    println("Shutting down actor system")
  }

}
