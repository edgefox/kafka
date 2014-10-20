package kafka.network

import kafka.network.ssl.SSLChannelFactory

object ChannelType {
  def getChannelType(name: String): ChannelType = {
    name.toLowerCase match {
      case PlaintextChannelType.name => PlaintextChannelType
      case SSLChannelType.name => SSLChannelType
      case _ => throw new RuntimeException("%s is an unknown channel type".format(name))
    }
  }

  def available() = Set(PlaintextChannelType, SSLChannelType)
}

sealed trait ChannelType { def name: String; def factory: ChannelFactory }


case object PlaintextChannelType extends ChannelType {
  val name = "plaintext"
  override val factory = PlainSocketChannelFactory
}

case object SSLChannelType extends ChannelType {
  val name = "ssl"
  override val factory = SSLChannelFactory
}