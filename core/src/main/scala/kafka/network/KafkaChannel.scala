package kafka.network

import java.net.{SocketAddress, SocketOption, Socket}
import java.nio.ByteBuffer
import java.nio.channels.{SelectionKey, Selector, SocketChannel}
import java.util

class KafkaChannel(val underlying: SocketChannel) extends SocketChannel(underlying.provider()) {
  def shutdownInput(): SocketChannel = underlying.shutdownInput()

  def isConnectionPending: Boolean = underlying.isConnectionPending

  def socket(): Socket = underlying.socket()

  def setOption[T](name: SocketOption[T], value: T): SocketChannel = underlying.setOption(name, value)

  def write(src: ByteBuffer): Int = underlying.write(src)

  def write(srcs: Array[ByteBuffer], offset: Int, length: Int): Long = underlying.write(srcs, offset, length)

  def isConnected: Boolean = underlying.isConnected

  def getRemoteAddress: SocketAddress = underlying.getRemoteAddress

  def finishConnect(): Boolean = underlying.finishConnect()

  def read(dst: ByteBuffer): Int = underlying.read(dst)

  def read(dsts: Array[ByteBuffer], offset: Int, length: Int): Long = underlying.read(dsts, offset, length)

  def connect(remote: SocketAddress): Boolean = underlying.connect(remote)

  def bind(local: SocketAddress): SocketChannel = underlying.bind(local)

  def shutdownOutput(): SocketChannel = underlying.shutdownOutput()

  def getLocalAddress: SocketAddress = underlying.getLocalAddress

  def getOption[T](name: SocketOption[T]): T = underlying.getOption(name)

  def supportedOptions(): util.Set[SocketOption[_]] = underlying.supportedOptions()

  protected def implCloseSelectableChannel() = underlying.close()

  protected def implConfigureBlocking(block: Boolean) = underlying.configureBlocking(block)

  def forKey(key: SelectionKey, selectOption: Int = -1): KafkaChannel = this

  def isExtraReadRequired: Boolean = false

  def registerSelector(selector: Selector, opt: Int) {
    underlying.register(selector, opt)
  }
}
