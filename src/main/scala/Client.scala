import Client._
import Server.{Join}
import akka.actor.{Actor, ActorRef, ActorSelection, Terminated}
import akka.pattern.ask
import akka.remote.DisassociatedEvent
import akka.util.Timeout
import scalafx.application.Platform

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random

class Client extends Actor{
  implicit val mytimeout = new Timeout(10 seconds)
  context.system.eventStream.subscribe(self, classOf[akka.remote.DisassociatedEvent])
  var serverRef: ActorSelection = null

  def receive ={

    case StartJoin(server, port, name)=>
      serverRef = context.actorSelection(s"akka.tcp://ball@$server:$port/user/server")
      val result  = serverRef ? Join(self, name)
      result.foreach( x => {
        if(x == Client.Joined){
          Platform.runLater {
            MainApp.clientcontroller.displayJoinStatus("You have Joined")
          }
          context.become(joined)
        } else {
          Platform.runLater {
            MainApp.clientcontroller.displayJoinStatus("Error")
          }
        }
      })

    case _=>
  }

  def joined: Receive ={

    case StartJoin(x, y, z)=>
      Platform.runLater {
        MainApp.clientcontroller.displayJoinStatus("You have already Joined")
      }

    case Begin =>
      Platform.runLater {
        MainApp.showGamePage()
      }
      sender ! "done"

    case DisassociatedEvent(local, remote, _) =>
      Platform.runLater {
        MainApp.showDialog("disconnect")
      }
      context.unbecome()

    case Question(ques) =>
      Platform.runLater{
        MainApp.gamecontroller.displayQuestion(ques)
      }

    case Hint(hintval) =>
      Platform.runLater{
        MainApp.gamecontroller.displayHint("There are "+hintval+" characters in this word")
      }

    case getAnswer(ans) =>
      Platform.runLater{
        MainApp.gamecontroller.getAnswer(ans)
      }

    case nextRound(round) =>

      val random = new Random()
      var num = random.nextInt(Server.definition.length)
      var ques = Server.definition(num)
      var key = Server.answers.filter(_._2 == ques).map(_._1)
      var hintval = key.head.length
      var ans = key.head

      Platform.runLater{
        MainApp.gamecontroller.displayQuestion(ques)
      }
      Platform.runLater{
        MainApp.gamecontroller.getAnswer(ans)
      }
      Platform.runLater{
        MainApp.gamecontroller.displayHint("There are "+hintval+" characters in this word")
      }
      Platform.runLater{
        MainApp.gamecontroller.displayRounds((round+1).toString)
      }

    case foundWinner =>
      Platform.runLater {
        MainApp.showDialog("winner")
      }
      context.unbecome()

    case _=>
      println("discard message")
  }
}


object Client {

  case object Joined
  case class StartJoin(serverIp: String, port: String,name: String)
  case object Begin
  case class Members(member: Iterable[Person])
  case class Question(q: String)
  case class Hint(h: String)
  case class getAnswer(a:String)
  case class nextRound(round: Int)
  case object foundWinner
  case object notifywinner
}