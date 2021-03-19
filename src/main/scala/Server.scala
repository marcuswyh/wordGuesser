import Client._
import Server.{Join, Start}
import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.remote.DisassociatedEvent
import akka.util.Timeout
import scalafx.collections.ObservableHashSet

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random


case class Person(ref: ActorRef, name: String){
  override def toString: String = {
    name
  }
}

class Server extends Actor{

  var winner_flag = false
  implicit val timeout: Timeout = Timeout(10 second)
  context.system.eventStream.subscribe(self, classOf[akka.remote.DisassociatedEvent])

  def receive ={
    case Join(my, name) =>
      Server.players += new Person(my, name)
      println(Server.players)
      sender() ! Joined

    case Start=>
      context.become(started)

      val results = for(client <- Server.players) yield {
        client.ref ? Begin
      }


      /** to prevent operations to be performed on clients that have connected after game starts*/
      val begintasks = Future {
        var i = 0
        for (result <- results) {
          for (value <- result) {
            i = i + 1
          }
        }
      }

      val random = new Random()
      var num = random.nextInt(Server.definition.length)
      var ques = Server.definition(num)
      var key = Server.answers.filter(_._2 == ques).map(_._1)
      var hintval = key.head.length
      var ans = key.head

      begintasks foreach { x =>
        for (client <- Server.players.map(_.ref)){
          client ! Question(ques)
          client ! Hint(hintval.toString)
          client ! getAnswer(ans)
        }
      }
    case _=>
  }


  def started: Receive = {
    case DisassociatedEvent(local, remote,_) =>

      for (client <- Server.players) yield{
        client.ref ! foundWinner
      }
      context.unbecome()
      Server.players.clear()
      MainApp.reshowHost()

    case _=>
  }
}


object Server {
  val players = new ObservableHashSet[Person]

  val answers = Map("test" -> "Used to assess the results of something",
                    "run" -> "A fast mode of moving from one place to another",
                    "eat" -> "To consume, usually sustenance",
                    "chicken" -> "An animal of the bird species, categorized as poultry",
                    "cancer" -> "Used to describe toxic people",
    "cow" -> "An animal of the mammal species, produces milk that we usually consume",
    "vegetable" -> "A plant or part of a plant that is used as food",
    "milk" -> "A liquid extracted from an animal by squeezing the teat of the animal",
    "egg" -> "A food that is laid from chickens",
    "bread" -> "A food that is made from flour, egg, and yeast then baked. Aka mian bao",
    "computer" -> "An device used for computing",
    "house" -> "A shelter housing families or people that is built with bricks and cement",
    "candy" -> "A sweet food product that can cause cavities",
    "wallet" -> "A product that is used to keep money and cards",
    "shoes" -> "A fashion accessory worn to protect/cover feet",
    "grass" -> "Vegetation consisting of short plants and leaves. Seen on lawns or gardens",
    "tree" -> "A plant that is characterized as having branches, trunks and leaves",
    "sky" -> "The region of the atmosphere observed from ground level",
    "cloud" -> "Condensation of water that floats in the sky",
    "leaf" -> "A part of a plant that is green",
    "pen" -> "A tool used for writing. Contains ink.",
    "phone" -> "A device used for telecommunication",
    "tissue" -> "A thin product akin to paper that is used for wiping",
    "whiteboard" -> "Used to be written on by markers. White in color",
    "water" -> "A liquid that is essential to life",
    "rain" -> "Phenomenon where droplets of water falls from the sky",
    "fan" -> "A device with blades that spins fast to produce wind",
    "table" -> "A furniture to put things on.",
    "chair" -> "A furniture to allow to be sit on")

  val definition: List[String] = List("Used to assess the results of something",
    "A fast mode of moving from one place to another",
    "To consume, usually sustenance",
    "An animal of the bird species, categorized as poultry",
    "Used to describe toxic people",
    "An animal of the mammal species, produces milk that we usually consume",
    "A plant or part of a plant that is used as food",
    "A liquid extracted from an animal by squeezing the teat of the animal",
    "A food that is laid from chickens",
    "A food that is made from flour, egg, and yeast then baked. Aka mian bao",
    "An device used for computing",
    "A shelter housing families or people that is built with bricks and cement",
    "A sweet food product that can cause cavities",
    "A product that is used to keep money and cards",
    "A fashion accessory worn to protect/cover feet",
    "Vegetation consisting of short plants and leaves. Seen on lawns or gardens",
    "A plant that is characterized as having branches, trunks and leaves",
    "The region of the atmosphere observed from ground level",
    "Condensation of water that floats in the sky",
    "A part of a plant that is green",
    "A tool used for writing. Contains ink.",
    "A device used for telecommunication",
    "A thin product akin to paper that is used for wiping",
    "Used to be written on by markers. White in color",
    "A liquid that is essential to life",
    "Phenomenon where droplets of water falls from the sky",
    "A device with blades that spins fast to produce wind",
    "A furniture to put things on.",
     "A furniture to allow to be sit on")


  //ask
  case class Join(myref: ActorRef, name: String)
  case object Start
  case class answer(a: String)
}