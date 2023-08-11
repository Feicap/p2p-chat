package com.tf.chat24

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe}



class ChatGroup(mainController: MainController) extends Actor with ActorLogging {
  val mediator: ActorRef = DistributedPubSub(context.system).mediator

  mediator ! Subscribe("ChatGroup", self)

  override def receive: Receive = {
    case MsgPublish(msg, name, rusname) =>
      if (rusname != ""){
        val newname = rusname
        mainController.printMessage(msg, newname)
      }
      else {
      mainController.printMessage(msg, name)
      }

    case AddToUserList(name) =>
      mainController.addUserToList(name)

    case RemoveUserFromList(name) =>
      mainController.RemoveUserFromList(name)

  }
}

class Publish extends Actor {
  val mediator: ActorRef = DistributedPubSub(context.system).mediator

  override def receive: Receive = {
    case MsgPublish(msg, name, rusname) =>
      mediator ! Publish("ChatGroup", MsgPublish(msg, name, rusname))

    case AddToUserList(name) =>
      mediator ! Publish("ChatGroup", AddToUserList(name))

    case RemoveUserFromList(name) =>
      mediator ! Publish("ChatGroup", RemoveUserFromList(name))

  }
}