package com.tf.chat24

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Put, Send}

class PrivateChatDestination(msgSender: MsgSender) extends Actor with ActorLogging{
  val mediator: ActorRef = DistributedPubSub(context.system).mediator
  mediator ! Put(self)

  override def receive: Receive = {
    case SndPrvMsg(msg, name) =>
      msgSender.printMessage(msg, name)
  }
}

class PrivateChatSender extends Actor with ActorLogging{
  val mediator: ActorRef = DistributedPubSub(context.system).mediator

  override def receive: Receive = {
    case PrivateMsg(msg, name, sender) =>
      mediator ! Send(path = s"/user/$name", SndPrvMsg(msg, sender), localAffinity = true)
  }
}