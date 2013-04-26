package sdg

import akka.actor.{Terminated, Actor, ActorRef}
import scala.collection.mutable.ArrayBuffer

/**
 * Created with IntelliJ IDEA.
 * User: vkostov
 * Date: 4/26/13
 * Time: 12:17 PM
 */
object Reaper {
  // Used by others to register an Actor for watching
  case class WatchMe(ref: ActorRef)
}

abstract class Reaper extends Actor {
  import Reaper._

  // Keep track of what we are watching
  val watched = ArrayBuffer.empty[ActorRef]

  // Derivations need to implement this method.  It's the
  // hook that's called when everything's dead
  def allSoulsReaped(): Unit

  //Watch and check for termination
  final def receive = {
    case WatchMe(ref) =>
      context.watch(ref)
      watched += ref
      println("Watching actor: " + ref)
    case Terminated(ref) =>
      watched -= ref
      println("Terminated actor: " + ref)
      if(watched.isEmpty) allSoulsReaped()
  }
}

class ProductionReaper extends Reaper {
  // Shutdown
  def allSoulsReaped(): Unit = {
    println("Shutting down...")
    context.system.shutdown()
  }
}
