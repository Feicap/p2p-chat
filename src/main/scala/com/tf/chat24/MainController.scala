package com.tf.chat24

import akka.actor.ActorRef
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.{Button, ListCell, ListView, Tab, TabPane, TextField}
import javafx.stage.Stage
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{HBox, VBox}
import com.ibm.icu.text.Transliterator
import javafx.scene.paint.Color
import javafx.scene.text.{Text, TextFlow}

import java.io.{BufferedWriter, FileOutputStream, OutputStreamWriter}
import java.nio.file.{Files, Paths}
import scala.io.Source
class MainController(chatUI: ChatUI) {

  private var clusterChat: ClusterChat = _
  private var actors: Array[ActorRef] = Array.empty
  private var senderPrivateMessages: ActorRef = _
  private var publish: ActorRef = _
  private var str = ""
  private var indexString = 0
  var stage: Stage = _
  var name = ""
  var rusname = ""
  private var ip = ""
  private var port = ""
  var currentUser: String = _

  def setChatUI(chatuiip: String, chatuiport: String, chatuiname: String, chatuistage: Stage): Unit = {
    name = chatuiname

    if (hasRussianCharacters(name) == true) {
      println(name)
      rusname = name
      name = transliterateRussianToEnglish(rusname)
    }

    ip = chatuiip
    port = chatuiport
    stage = chatuistage
    akka(ip, port, name)
    if (rusname == ""){publish ! AddToUserList(name)}else{publish ! AddToUserList(rusname)}

  }

  @FXML
  var userList: ListView[String] = _
  @FXML
  var enterButton: Button = _
  @FXML
  var textFlow: TextFlow = _
  @FXML
  var inputField: TextField = _
  @FXML
  var TabPane: TabPane = _

  def transliterateRussianToEnglish(russianName: String): String = {
    val transliterator = Transliterator.getInstance("Russian-Latin/BGN")
    transliterator.transliterate(russianName)
  }
  def hasRussianCharacters(text: String): Boolean = {
    val russianPattern = "[а-яА-Я]".r
    russianPattern.findFirstIn(text).isDefined
  }


  var privateinputField: TextField = _
  var privatetextFlow: TextFlow = _

  def this() = this(new ChatUI)

  @FXML
  def initialize(): Unit = {
      enterButton.setOnAction((event: ActionEvent) => {
        sendMessage(event)
      })
  }

    def akka(ip:String, port:String, name:String): Unit = {
      clusterChat = new ClusterChat(ip, port)

      while (!clusterChat.getCheckMemberStatus) {
        Thread.sleep(1000)
      }
      actors = clusterChat.createActors(name, this)
      publish = actors(1)
      val onClose = new OnClose( this, publish)
      senderPrivateMessages = actors(3)
      Thread.sleep(100)
    }


    def printMessage(str: String, namez: String): Unit = {
      Platform.runLater(() => {
        if (hasRussianCharacters(namez) == false){
        if (name == namez){

        val message = new Text(str + "\n")
        var nameText = new Text()

        nameText = new Text(s"$namez: ")
        nameText.setFill(Color.RED)
        textFlow.getChildren.addAll(nameText, message)
        }
        else{
        val message = new Text(str + "\n")
        var nameText = new Text()
        nameText = new Text(s"$namez: ")
        textFlow.getChildren.addAll(nameText, message)
        }}else{
          if (rusname == namez) {

            val message = new Text(str + "\n")
            var nameText = new Text()

            nameText = new Text(s"$namez: ")
            nameText.setFill(Color.RED)
            textFlow.getChildren.addAll(nameText, message)
          }
          else {
            val message = new Text(str + "\n")
            var nameText = new Text()
            nameText = new Text(s"$namez: ")
            textFlow.getChildren.addAll(nameText, message)
          }
        }
      })
    }

  def printPrivateMessage(str: String, namez: String): Unit = {
    Platform.runLater(() => {
      println("printPrivateMessage test " + namez)
      if (hasRussianCharacters(namez) == false){
      if (name == namez){
      val message = new Text(str + "\n")
      val nameText = new Text(s"$namez: ")
      nameText.setFill(Color.RED)
      privatetextFlow.getChildren.addAll(nameText, message)
      }else{
        val message = new Text(str + "\n")
        val nameText = new Text(s"$namez: ")
        privatetextFlow.getChildren.addAll(nameText, message)
      }}else{
        println("printPrivateMessage ru test "+ rusname , namez)
        if (rusname == namez) {
          val message = new Text(str + "\n")
          val nameText = new Text(s"$namez: ")
          nameText.setFill(Color.RED)
          privatetextFlow.getChildren.addAll(nameText, message)
        } else {
          val message = new Text(str + "\n")
          val nameText = new Text(s"$namez: ")
          privatetextFlow.getChildren.addAll(nameText, message)
      }
      }
    })
  }

    def sendMessage(event: ActionEvent): Unit ={
      val selectedTab = TabPane.getSelectionModel.getSelectedItem
      val tabId = selectedTab.getId
      val message = inputField.getText()

      if (tabId == "PrivateTab") {
        val tabText = selectedTab.getText()
        val message = privateinputField.getText
          var username = tabText.substring(6)
        if (hasRussianCharacters(username) == true) {
          username = transliterateRussianToEnglish(username)
        }
        senderPrivateMessages ! PrivateMsg(message, username, name, rusname) //лс
        if (rusname != ""){printPrivateMessage( message, rusname )}else{printPrivateMessage( message, name )}

        privateinputField.clear()
      } else {
        publish ! MsgPublish(message, name, rusname) //общий чат
      }
      inputField.clear()
    }


  def createTab(username: String): Unit = {
    Platform.runLater(() => {
      // Проверка наличия вкладки с таким же текстом
      val existingTab = TabPane.getTabs.stream().filter(tab => tab.getText == "Чат с " + username).findFirst().orElse(null)
      if (existingTab != null) {
        TabPane.getSelectionModel.select(existingTab)
        return
      }

      val tab = new Tab()
      tab.setId(s"PrivateTab")
      tab.setClosable(true)
      tab.setText("Чат с " + username)

      val content = new VBox()
      val defVbox = new VBox()
      defVbox.setId("defVbox")
      defVbox.setPrefHeight(439.0)
      defVbox.setPrefWidth(400.0)

      val chatArea = new HBox()
      chatArea.setSpacing(10.0)
      val privatedtextFlow = new TextFlow()
      privatedtextFlow.setId("privatetextArea")
      privatedtextFlow.setPrefHeight(400.0)
      privatedtextFlow.setPrefWidth(496.0)

      chatArea.getChildren.add(privatedtextFlow)

      val inputArea = new HBox()
      inputArea.setSpacing(10.0)
      val privatedinputField = new TextField()
      privatedinputField.setId("privateinputField")
      privatedinputField.setPrefColumnCount(30)
      privatedinputField.setPrefHeight(25.0)
      privatedinputField.setPrefWidth(437.0)
      val privateenterButton = new Button()
      privateenterButton.setId("privateenterButton")
      privateenterButton.setText("Enter")

      inputArea.getChildren.add(privatedinputField)
      inputArea.getChildren.add(privateenterButton)

      defVbox.getChildren.add(chatArea)
      defVbox.getChildren.add(inputArea)

      content.getChildren.add(defVbox)

      tab.setContent(content)

      TabPane.getTabs.add(tab)
      privateinputField = privatedinputField
      privatetextFlow = privatedtextFlow

      Platform.runLater(() =>
        privateenterButton.setOnAction((event: ActionEvent) => {
          sendMessage(event)
        }))
    })
  }


Platform.runLater(() => {
  userList.setOnMouseClicked((event: MouseEvent) => {
    if (event.getClickCount == 2) {
      val selectedUsername = userList.getSelectionModel.getSelectedItem
      if (selectedUsername != null && name != selectedUsername && rusname != selectedUsername) {
      createTab(selectedUsername)
      }
    }
  })
})

  def addUserToList(name: String): Unit = {
    val filename = "userlist.txt"
    val fileContent = try {
      new String(Files.readAllBytes(Paths.get(filename)))
    } catch {
      case _: Exception => ""
    }
    val updatedContent = if (!fileContent.contains(name)) {
      fileContent + name + "\n"
    } else {
      fileContent
    }
    Files.write(Paths.get(filename), updatedContent.getBytes)
    val lines = updatedContent.split("\n")
    if (rusname == ""){
    currentUser = MainController.this.name
    }else{
      currentUser = MainController.this.rusname
    }
    Platform.runLater(() => {
      userList.getItems.clear()
      userList.getItems.addAll(lines.distinct: _*) // Добавить только новые имена

      userList.setCellFactory((_: ListView[String]) => new ListCell[String]() {
        override def updateItem(item: String, empty: Boolean): Unit = {
          super.updateItem(item, empty)
          if (item != null && !empty) {
            println(item, currentUser, name)
            if (item == currentUser) {
              setTextFill(javafx.scene.paint.Color.RED)
            } else {
              setTextFill(javafx.scene.paint.Color.BLACK)
            }
            setText(item)
          } else {
            setText(null)
          }
        }
      })

    })
  }

  //удаление при onClose пользователя
  def RemoveUserFromList(name: String): Unit = {
    var namequit = name

    if (currentUser == rusname){
      namequit = currentUser
    }
    val filename = "userlist.txt"
    val fileContent = try {
      new String(Files.readAllBytes(Paths.get(filename)))
    } catch {


      case _: Exception => ""
    }

    val updatedContent = fileContent.split("\n").filterNot(_.equals(namequit)).mkString("\n") + "\n"
    val lines = updatedContent.split("\n")
    if (lines.length >= 1) {
      Files.write(Paths.get(filename), updatedContent.getBytes)
    } else {
      Files.write(Paths.get(filename), "".getBytes)
    }


    Platform.runLater(() => {
      userList.getItems.clear()
      userList.getItems.addAll(lines.distinct: _*)
    })

  }





}