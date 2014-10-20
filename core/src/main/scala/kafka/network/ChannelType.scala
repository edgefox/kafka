package kafka.network

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

sealed trait ChannelType { def name: String }


case object PlaintextChannelType extends ChannelType {
  val name = "plaintext"
}

case object SSLChannelType extends ChannelType {
  val name = "ssl"
}