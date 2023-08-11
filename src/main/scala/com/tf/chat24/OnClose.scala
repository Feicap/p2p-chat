package com.tf.chat24

import akka.actor.ActorRef
import javafx.application.Platform
import javafx.stage.Stage
//логика закрытия программы
class OnClose(mainController: MainController, publish: ActorRef) {

  mainController.stage.setOnCloseRequest(_ => {
    publish ! RemoveUserFromList(mainController.name)
    Platform.exit()
    System.exit(0)
  })
}
