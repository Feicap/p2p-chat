package com.tf.chat24

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Put, Send}

class PrivateChatDestination(mainController: MainController) extends Actor with ActorLogging{
  val mediator: ActorRef = DistributedPubSub(context.system).mediator
  mediator ! Put(self)

  override def receive: Receive = {
    case SndPrvMsg(msg, name, rusname) =>
      if (rusname != ""){
        val newname = rusname
        println(msg, newname)
        mainController.printPrivateMessage(msg, newname)
      }else{

        mainController.printPrivateMessage(msg, name)
      }


    case CrPrvTab(sender) =>
      mainController.createTab(sender)

  }
}

class PrivateChatSender extends Actor with ActorLogging{
  val mediator: ActorRef = DistributedPubSub(context.system).mediator
  override def receive: Receive = {
    case PrivateMsg(msg, name, sender, rusname) =>
      if (rusname != ""){
        mediator ! Send(path = s"/user/$name", CrPrvTab(rusname), localAffinity = true)
        mediator ! Send(path = s"/user/$name", SndPrvMsg(msg, sender, rusname), localAffinity = true)
      }else{
        mediator ! Send(path = s"/user/$name", CrPrvTab(sender), localAffinity = true)
        mediator ! Send(path = s"/user/$name", SndPrvMsg(msg, sender, rusname), localAffinity = true)
      }

  }
}