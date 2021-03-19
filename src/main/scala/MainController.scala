import Server.Start
import akka.actor.ActorRef
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.scene.control.{Button, Label, ListView, TextField}
import scalafx.scene.shape.Circle
import scalafxml.core.macros.sfxml

import MainApp._

@sfxml
class MainController(private val hostBtn: Button,
                     private val joinBtn: Button) {


  var clientActorRef: Option[ActorRef] = None

  def openHost(action :ActionEvent): Unit ={
    MainApp.showHost()
  }

  def openClient(action :ActionEvent): Unit ={
    MainApp.showClient()
  }
}
