package kafka.network

object ConnectionType {
  def getConnectionType(name: String): ConnectionType = {
    name.toLowerCase match {
      case PlaintextConnectionType.name => PlaintextConnectionType
      case SSLConnectionType.name => SSLConnectionType
      case _ => throw new RuntimeException("%s is an unknown connection type".format(name))
    }
  }

  def available() = Set(PlaintextConnectionType, SSLConnectionType)
}

sealed trait ConnectionType { def name: String }

case object PlaintextConnectionType extends ConnectionType {
  val name = "plaintext"
}

case object SSLConnectionType extends ConnectionType {
  val name = "ssl"
}