import java.net.NetworkInterface

import akka.actor.{ActorSystem, ExtendedActorSystem, Extension, ExtensionId, Props}
import com.typesafe.config.ConfigFactory
import scalafx.application.{JFXApp, Platform}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafxml.core.{FXMLLoader, NoDependencyResolver}
import javafx.scene.{layout => jfsl}
import scalafx.Includes._
import scalafx.scene.control.{Alert, ButtonType}
import scalafx.scene.control.Alert.AlertType

import scala.collection.JavaConverters._

class AddressExtension(system: ExtendedActorSystem) extends Extension {
  val address = system.provider.getDefaultAddress
}

object AddressExtension extends ExtensionId[AddressExtension] {
  def createExtension(system: ExtendedActorSystem): AddressExtension = new AddressExtension(system)

  def hostOf(system: ActorSystem): String = AddressExtension(system).address.host.getOrElse("")
  def portOf(system: ActorSystem): Int    = AddressExtension(system).address.port.getOrElse(0)
}

object MainApp extends JFXApp {
  var count = -1
  val addresses = (for (inf <- NetworkInterface.getNetworkInterfaces.asScala;
                        add <- inf.getInetAddresses.asScala) yield {
    count = count + 1
    (count -> add)
  }).toMap

  val ipaddress = addresses(2)

  val overrideConf = ConfigFactory.parseString(
    s"""
       |akka {
       |  loglevel = "INFO"
       |
 |  actor {
       |    provider = "akka.remote.RemoteActorRefProvider"
       |  }
       |
 |  remote {
       |    enabled-transports = ["akka.remote.netty.tcp"]
       |    netty.tcp {
       |      hostname = "${ipaddress.getHostAddress}"
       |      port = 0
       |    }
       |
 |    log-sent-messages = on
       |    log-received-messages = on
       |  }
       |
 |}
       |
     """.stripMargin)

  val myConf = overrideConf.withFallback(ConfigFactory.load())
  val system = ActorSystem("ball", myConf)

  val currentport = AddressExtension.portOf(system)
  val currentip = AddressExtension.hostOf(system)

  //create server actor
  val serverRef = system.actorOf(Props[Server](), "server")
  //create client actor
  val clientRef = system.actorOf(Props[Client], "client")



  val loader = new FXMLLoader(null, NoDependencyResolver)
  loader.load(getClass.getResourceAsStream("MainPage.fxml"))

  val borderPane: jfsl.BorderPane = loader.getRoot[jfsl.BorderPane]

  /** initialize client loader and client controller instance*/
  val clientloader = new FXMLLoader(null, NoDependencyResolver)
  clientloader.load(getClass.getResourceAsStream("Client.fxml"))
  val clientcontroller = clientloader.getController[ClientController#Controller]()
  clientcontroller.clientActorRef = Option(clientRef)

  /** initialize host loader and client controller instance */
  val hostloader = new FXMLLoader(null, NoDependencyResolver)
  hostloader.load(getClass.getResourceAsStream("Host.fxml"))
  val hostcontroller = hostloader.getController[HostController#Controller]()
  hostcontroller.clientActorRef = Option(clientRef)

  /** initialize game loader and game controller instance */
  val gameloader = new FXMLLoader(null, NoDependencyResolver)
  gameloader.load(getClass.getResourceAsStream("GamePage.fxml"))
  val gamecontroller = gameloader.getController[GameController#Controller]()
  gamecontroller.clientActorRef = Option(clientRef)

  stage = new PrimaryStage{
    scene = new Scene(){
      root = borderPane
    }
  }

  stage.onCloseRequest = handle {
    system.terminate
  }

  def showHost()={
    val roots = hostloader.getRoot[jfsl.AnchorPane]
    this.borderPane.setCenter(roots)
  }

  def showClient()={
    val roots = clientloader.getRoot[jfsl.AnchorPane]
    this.borderPane.setCenter(roots)
  }

  def reshowHost()={
    val roots = hostloader.getRoot[jfsl.AnchorPane]
    borderPane.setCenter(roots)
  }

  def reshowClient()={
    val roots = clientloader.getRoot[jfsl.AnchorPane]
    borderPane.setCenter(roots)
  }

  def showGamePage()={
    val roots = gameloader.getRoot[jfsl.AnchorPane]
    borderPane.setCenter(roots)
  }

  def showDialog(message: String): Unit ={
    if (message == "disconnect"){
      Platform.runLater {
        val alert = new Alert(AlertType.Confirmation) {
          initOwner(stage)
          title = "Error detected!"
          headerText = "Disconnection detected"
          contentText = "Server has disconnected from game. Game will now end."
        }
        val result = alert.showAndWait()

        // React to user's selectioon
        result match {
          case Some(ButtonType.OK) => stage.close()
            system.terminate()
          case _                   => stage.close()
            system.terminate()
        }
      }
    }

    if (message == "winner"){
      Platform.runLater {
        val alert2 = new Alert(AlertType.Confirmation) {
          initOwner(stage)
          title = "Game Over!"
          headerText = "Winner Found!"
          contentText = "A player has finished before you. Game will now end."
        }
        val result = alert2.showAndWait()

        // React to user's selectioon
        result match {
          case Some(ButtonType.OK) => stage.close()
            system.terminate()
          case _                   => stage.close()
            system.terminate()
        }
      }
    }

  }

  def showWinnerDialog(message: String): Unit ={
    Platform.runLater {
      val alert = new Alert(AlertType.Confirmation) {
        initOwner(stage)
        title = "Game Ended!"
        headerText = "Congratulations!"
        contentText = message + " Game will end now."
      }
      val result = alert.showAndWait()

      // React to user's selectioon
      result match {
        case Some(ButtonType.OK) =>
          stage.close()
          system.terminate()
        case _                   => stage.close()
          system.terminate()
      }
    }
  }
}
