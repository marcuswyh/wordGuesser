import Client.StartJoin
import Server.Start
import akka.actor.ActorRef
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.scene.control.{Button, Label, ListView, TextField}
import scalafx.scene.shape.Circle
import scalafxml.core.macros.sfxml

@sfxml
class ClientController(private val joinBtn: Button,
                       private val portTxt: TextField,
                       private val addressTxt: TextField,
                       private val usernameTxt: TextField,
                       private val statusLbl: Label) {

  var clientActorRef: Option[ActorRef] = None

  statusLbl.text = "No host joined"

  def handleJoin(actionEvent: ActionEvent) {
    //ask the client actor to joined the server based on IP
    clientActorRef foreach { ref =>
      ref ! StartJoin(addressTxt.text.value, portTxt.text.value, usernameTxt.text.value)
    }
  }

  def displayJoinStatus(text: String): Unit = {
    statusLbl.text = text
  }

}
