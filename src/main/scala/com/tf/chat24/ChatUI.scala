package com.tf.chat24

import javafx.application.{Application, Platform}
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.fxml.FXMLLoader
import javafx.scene.Parent

class ChatUI extends Application {
  var ip: String = ""
  var port: String = ""
  var name: String = ""

  override def start(stage: Stage): Unit = {
    val loader = new FXMLLoader(getClass.getResource("/login.fxml"))
    val root: Parent = loader.load()

    stage.setTitle("Логин")
    stage.setScene(new Scene(root))
    stage.show()

    stage.setOnCloseRequest(_ => {
      Platform.exit()
      System.exit(0)
    })
  }

  def mainwindow(stage: Stage): Unit = {
    val loader = new FXMLLoader(getClass.getResource("/main.fxml"))
    val root: Parent = loader.load()

    val mainController = loader.getController.asInstanceOf[MainController]
    mainController.setChatUI(ip, port, name, stage)

    stage.setTitle("Чат-24")
    stage.setScene(new Scene(root))
    stage.show()


  }

}