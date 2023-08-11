package com.tf.chat24

import javafx.fxml.{FXML}
import javafx.scene.control.{Alert, Button, TextField}
import javafx.stage.Stage
import javafx.event.ActionEvent

class LoginController(chatUI: ChatUI) {
  var name = ""


  @FXML
  var ipField: TextField = _
  @FXML
  var portField: TextField = _
  @FXML
  var nameField: TextField = _
  @FXML
  var loginButton: Button = _

  def this() = this(new ChatUI)



  @FXML
  def initialize(): Unit = {
    loginButton.setOnAction((event: ActionEvent) => handleLoginButton(event))
  }

  def handleLoginButton(event: ActionEvent): Unit = {
    val ip = ipField.getText
    val port = portField.getText
    name = nameField.getText

    if (ip.isEmpty || port.isEmpty || name.isEmpty) {
      val alert = new Alert(Alert.AlertType.ERROR)
      alert.setTitle("Ошибка")
      alert.setHeaderText(null)
      alert.setContentText("Заполните все поля")
      alert.showAndWait()
    } else {
      chatUI.ip = ip
      chatUI.port = port
      chatUI.name = name
//      val mainController = new MainController(this)
//      mainController.start(new Stage())
      chatUI.mainwindow(new Stage)


      val stage: Stage = loginButton.getScene.getWindow.asInstanceOf[Stage]
      stage.close()
    }

    }
  }