package com.tf.chat24

import akka.actor.ActorRef
import javafx.application.Platform
import java.nio.file.{Files, Paths}


class MsgSender(chatUI: ChatUI) {
  private val text = chatUI.getTextArea
  private val field = chatUI.getTextField
  private val submit = chatUI.getButton
  var name = ""

  private var counter: Int = 0
  private var clusterChat: ClusterChat = _

  private var senderPrivateMessages: ActorRef = _
  private var str = ""
  private var actors: Array[ActorRef] = Array.empty
  private var publish: ActorRef = _
  private var indexString = 0
  text.setText("Введите port: 127.0.0.1:255(x) ")

  submit.setOnAction(e => {
    counter match {
      case 2 =>
        sendMessage()
      case 0 =>
        createActorSystem()
      case 1 =>
        createActors()
    }
  })

  def printMessage(str: String, name: String): Unit = {
    val message = name + ": " + str + "\n"
    text.appendText(message)
  }

  def createActorSystem(): Unit = {
    clusterChat = new ClusterChat(field.getText())
    field.clear()
    text.setText("Введите имя на английском ")
    counter += 1
  }

  def createActors(): Unit = {
    while (!clusterChat.getCheckMemberStatus) {
      Thread.sleep(1000)
    }
    name = field.getText()

    actors = clusterChat.createActors(name, this)
    publish = actors(1)
    val onClose = new OnClose(chatUI, this, publish)
    senderPrivateMessages = actors(3)
    field.clear()
    text.clear()
    counter += 1
    publish ! AddToUserList(name)
  }

  def sendMessage(): Unit ={
    str = field.getText()
    if (str.charAt(0).equals('@')) {
      indexString = str.indexOf(" ")
      senderPrivateMessages ! PrivateMsg(str.substring(indexString, str.length), str.substring(1, indexString), name) //лс
    } else {
      publish ! MsgPublish(str, name) //общий чат
    }
    field.clear()
  }

//список пользователей
  def addUserToList(name: String): Unit = {
    val userList = chatUI.userList
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

    Platform.runLater(() => {
      userList.getItems.clear()
      userList.getItems.addAll(lines.distinct: _*) // Добавить только новые имена
    })
  }
  //удаление при onClose пользователя
  def RemoveUserFromList(name: String): Unit = {
    val userList = chatUI.userList
    val filename = "userlist.txt"
    val fileContent = try {
      new String(Files.readAllBytes(Paths.get(filename)))
    } catch {
      case _: Exception => ""
    }
    val updatedContent = fileContent.split("\n").filterNot(_.trim == name).mkString("\n") + "\n"
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

