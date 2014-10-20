/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kafka.network

import java.net.InetSocketAddress
import java.nio.channels._
import kafka.utils.{nonthreadsafe, Logging}
import kafka.api.RequestOrResponse


object BlockingChannel {
  val UseDefaultBufferSize = -1
}

/**
 * A simple blocking channel with timeouts correctly enabled.
 *
 */
@nonthreadsafe
class BlockingChannel(val host: String,
                      val port: Int,
                      val channelType: ChannelType,
                      val readBufferSize: Int,
                      val writeBufferSize: Int,
                      val readTimeoutMs: Int) extends Logging {
  private var connected = false
  private var channel: SocketChannel = null
  private var readChannel: ReadableByteChannel = null
  private var writeChannel: GatheringByteChannel = null
  private val lock = new Object()

  def connect() = lock synchronized {
    if (!connected) {
      try {
        channel = channelType.factory.createClientChannel(host, port, readBufferSize, writeBufferSize, readTimeoutMs)
        channel.connect(new InetSocketAddress(host, port))

        writeChannel = channel
        readChannel = Channels.newChannel(channel.socket().getInputStream)
        connected = true
        // settings may not match what we requested above
        val msg = "Created socket with SO_TIMEOUT = %d (requested %d), SO_RCVBUF = %d (requested %d), SO_SNDBUF = %d (requested %d)."
        debug(msg.format(channel.socket.getSoTimeout,
                         readTimeoutMs,
                         channel.socket.getReceiveBufferSize,
                         readBufferSize,
                         channel.socket.getSendBufferSize,
                         writeBufferSize))
      } catch {
        case e: Throwable => disconnect()
      }
    }
  }

  def disconnect() = lock synchronized {
    if (channel != null) {
      swallow(channel.close())
      swallow(channel.socket.close())
      channel = null
      writeChannel = null
    }
    // closing the main socket channel *should* close the read channel
    // but let's do it to be sure.
    if (readChannel != null) {
      swallow(readChannel.close())
      readChannel = null
    }
    connected = false
  }

  def isConnected = connected

  def send(request: RequestOrResponse): Int = {
    if (!connected)
      throw new ClosedChannelException()

    val send = new BoundedByteBufferSend(request)
    send.writeCompletely(writeChannel)
  }

  def receive(): Receive = {
    if (!connected)
      throw new ClosedChannelException()

    val response = new BoundedByteBufferReceive()
    response.readCompletely(readChannel)

    response
  }

}
