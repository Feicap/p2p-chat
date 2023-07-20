package com.tf.chat24

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe}

class ChatGroup(msgSender: MsgSender) extends Actor with ActorLogging {
  val mediator: ActorRef = DistributedPubSub(context.system).mediator
  mediator ! Subscribe("ChatGroup", self)

  override def receive: Receive = {
    case MsgPublish(msg, name) =>
      msgSender.printMessage(msg, name)

    case AddToUserList(name) =>
      msgSender.addUserToList(name)

    case RemoveUserFromList(name) =>
      msgSender.RemoveUserFromList(name)

  }
}

class Publish extends Actor {
  val mediator: ActorRef = DistributedPubSub(context.system).mediator

  override def receive: Receive = {
    case MsgPublish(msg, name) =>
      mediator ! Publish("ChatGroup", MsgPublish(msg, name))

    case AddToUserList(name) =>
      mediator ! Publish("ChatGroup", AddToUserList(name))

    case RemoveUserFromList(name) =>
      mediator ! Publish("ChatGroup", RemoveUserFromList(name))

  }
}