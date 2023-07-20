package com.tf.chat24

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.Cluster
import com.typesafe.config.{Config, ConfigFactory}

class ClusterChat(port: String) {
  private val system: ActorSystem = createActorSystem
  private val cluster: Cluster = Cluster(system)
  private var actors: Array[ActorRef] = Array.empty
  private var check: Boolean = false
  private var name = ""

  cluster.registerOnMemberUp({
    check = true
  })

  def createActors(name: String, msgSender: MsgSender): Array[ActorRef] = {
    this.name = name
    actors = Array(
      system.actorOf(Props(classOf[ChatGroup], msgSender), port.toString),
      system.actorOf(Props[Publish], port.toString + "pub"),
      system.actorOf(Props(classOf[PrivateChatDestination], msgSender), name),
      system.actorOf(Props[PrivateChatSender]))
    actors
  }

  private def createActorSystem: ActorSystem = {
    val config: Config = ConfigFactory.parseString(s"""akka.remote.artery.canonical.port = "255$port"""")
      .withFallback(ConfigFactory.load())
    val system: ActorSystem = ActorSystem("Cluster", config)
    system
  }
  def getActorSystem: ActorSystem = system
  def getCheckMemberStatus: Boolean = check
}