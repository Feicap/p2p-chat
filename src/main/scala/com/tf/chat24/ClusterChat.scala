package com.tf.chat24

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.Cluster
import com.typesafe.config.{Config, ConfigFactory}


class ClusterChat(ip: String ,port: String) {
  private val system: ActorSystem = createActorSystem
  private val cluster: Cluster = Cluster(system)
  private var actors: Array[ActorRef] = Array.empty
  private var check: Boolean = false
  private var name = ""

  cluster.registerOnMemberUp({
    check = true
  })

  def createActors(name: String, mainController: MainController): Array[ActorRef] = {
    this.name = name
    actors = Array(
      system.actorOf(Props(classOf[ChatGroup], mainController),  port.toString),
      system.actorOf(Props[Publish], port.toString + "pub"),
      system.actorOf(Props(classOf[PrivateChatDestination], mainController), name),
      system.actorOf(Props[PrivateChatSender]))
    actors
  }

  private def createActorSystem: ActorSystem = {
    val config: Config = ConfigFactory.parseString(
      s"""akka.remote.artery.canonical.hostname = "$ip"
         |akka.remote.artery.canonical.port = $port
  """.stripMargin).withFallback(ConfigFactory.load())
    val system: ActorSystem = ActorSystem("Cluster", config)
    system
  }
  def getActorSystem: ActorSystem = system
  def getCheckMemberStatus: Boolean = check
}