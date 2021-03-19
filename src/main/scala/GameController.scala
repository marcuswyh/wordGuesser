
import Client.nextRound
import akka.actor.ActorRef
import scalafx.event.ActionEvent
import scalafx.scene.control.{Button, Label, TextField}
import scalafxml.core.macros.sfxml

@sfxml
class GameController (private val submitBtn: Button,
                      private var definitionLbl: Label,
                      private var hintLbl: Label,
                      private val answerTxt: TextField,
                      private val roundLbl: Label,
                      private val wrongstatusLbl: Label){

  var answer: String = ""
  roundLbl.text = "1"
  var clientActorRef: Option[ActorRef] = None

  def displayQuestion(text: String): Unit = {
    definitionLbl.text = text
  }

  def displayHint(text: String): Unit = {
    hintLbl.text = text
  }

  def displayRounds(text: String): Unit = {
    roundLbl.text = text
  }

  def handleAnswer(actionEvent: ActionEvent): Unit ={
    if (roundLbl.text.value.toInt <= 5){
      if (answerTxt.text.value.toLowerCase == answer){
        if (roundLbl.text.value.toInt == 5){
          MainApp.showWinnerDialog("You won!!")

        }
        else{
          wrongstatusLbl.visible = false
          clientActorRef foreach { ref =>
            ref ! nextRound(roundLbl.text.value.toInt)
          }
          answerTxt.text = ""
          answerTxt.requestFocus()
        }

      }
      else{
        println("WRONG ANSWER PLEASE TRY AGAIN")
        wrongstatusLbl.visible = true
        answerTxt.text = ""
        answerTxt.requestFocus()
      }
    }
  }

  def getAnswer(ans: String): Unit ={
    answer = ans
  }

  def showdialog: Unit ={
    MainApp.showDialog("winner")
  }
}
