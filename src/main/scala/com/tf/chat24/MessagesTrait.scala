package com.tf.chat24

trait Msg
case class MsgPublish(msg: String, name: String) extends Msg
case class PrivateMsg(msg: String, name: String, sender: String) extends Msg
case class SndPrvMsg(msg: String, name: String) extends Msg
case class AddToUserList(name: String) extends Msg
case class RemoveUserFromList(name: String) extends Msg
