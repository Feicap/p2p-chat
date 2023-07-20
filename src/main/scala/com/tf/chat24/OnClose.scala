package com.tf.chat24

import akka.actor.ActorRef
import javafx.application.Platform
import javafx.stage.Stage
//логика закрытия программы
class OnClose(chatUI: ChatUI, msgSender: MsgSender, publish: ActorRef) {
  val stage: Stage = chatUI.getStage
  val name = msgSender.name
  stage.setOnCloseRequest(e => {
    publish ! RemoveUserFromList(name)
    Platform.exit()
    System.exit(0)
  })
}
