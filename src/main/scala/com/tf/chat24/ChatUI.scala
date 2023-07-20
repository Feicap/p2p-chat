package com.tf.chat24

import javafx.application.{Application, Platform}
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control._
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout.{HBox, VBox}
import javafx.stage.Stage

class ChatUI extends Application {
  private val groupChatPane: VBox = new VBox()
  private val Messagefield: TextField = new TextField()
  private val submit: Button = new Button("Enter")
  private val text: TextArea = new TextArea()
  private var stage: Stage = _
  private val root: TabPane = new TabPane()
  private val scene: Scene = new Scene(root)
  private val ChatTab: Tab = new Tab("Чат")

  private val Sender = new MsgSender(this)
  Messagefield.setPrefColumnCount(30)
  text.setPrefSize(400, 400)
  text.setEditable(false)
  text.setWrapText(true)
  groupChatPane.setAlignment(Pos.CENTER)
  Messagefield.setPrefColumnCount(30)
  text.setPrefSize(400, 400)
  text.setEditable(false)
  text.setWrapText(true)
  val userList: ListView[String] = new ListView[String]()

  private val mainwindow = new HBox(text, userList) // Создаем HBox и добавляем в него text и userList
  mainwindow.setSpacing(10) // Устанавливаем промежуток между компонентами в HBox
  private val inputwindow = new HBox(Messagefield, submit)
  inputwindow.setSpacing(10)

  groupChatPane.getChildren.addAll(mainwindow, inputwindow)
  ChatTab.setContent(groupChatPane)
  ChatTab.setClosable(false)
  root.getTabs.add(ChatTab)

  Messagefield.setOnKeyPressed((e: KeyEvent) => {
    if (e.getCode.equals(KeyCode.ENTER))
      submit.fire()
  })

  override def start(primaryStage: Stage): Unit = {
    stage = primaryStage
    primaryStage.setTitle("Чат 24")
    primaryStage.setScene(scene)
    primaryStage.show()

    primaryStage.setOnCloseRequest(e => {
      Platform.exit()
      System.exit(0)
    })
  }
  def getTextArea: TextArea = text
  def getTextField: TextField = Messagefield
  def getButton: Button = submit
  def getStage: Stage = stage

}