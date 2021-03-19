import Client.{StartJoin}
import Server.Start
import akka.actor.ActorRef
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.scene.control.{Button, Label, ListView, TextField}
import scalafx.scene.shape.Circle
import scalafxml.core.macros.sfxml

@sfxml
class HostController(private val startBtn: Button,
                     private val portTxt: TextField,
                     private val addressTxt: TextField,
                     private val usernameTxt: TextField,
                     private val clientList: ListView[Person]) {

  var clientActorRef: Option[ActorRef] = None

  portTxt.text = MainApp.currentport.toString
  addressTxt.text = MainApp.currentip.toString

  Server.players.onChange((x, y) => {
    Platform.runLater {
      clientList.items = ObservableBuffer(x.toList)
    }
  })

  def handleJoin(actionEvent: ActionEvent): Unit ={
    clientActorRef foreach { ref =>
      ref ! StartJoin(addressTxt.text.value, portTxt.text.value, usernameTxt.text.value)
    }
  }

  def handleStart(actionEvent: ActionEvent): Unit ={
    MainApp.serverRef ! Start
  }

}
