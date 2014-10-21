/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kafka.network

import java.nio.channels.{SocketChannel, ServerSocketChannel}

import kafka.utils.Logging

abstract class ChannelFactory extends Logging {
  def createServerChannel(serverSocketChannel: ServerSocketChannel, recvBufferSize: Int, sendBufferSize: Int): KafkaChannel = {
    val channel = createServerChannelImpl(serverSocketChannel)
    configureServerChannel(channel, recvBufferSize, sendBufferSize)
    channel
  }
  
  def createClientChannel(host: String, port: Int, readBufferSize: Int, writeBufferSize: Int, readTimeoutMs: Int): SocketChannel = {
    val channel = createClientChannelImpl(host, port)
    configureClientChannel(channel, readBufferSize, writeBufferSize, readTimeoutMs)
    channel
  }

  protected def createServerChannelImpl(serverSocketChannel: ServerSocketChannel): KafkaChannel

  protected def createClientChannelImpl(host: String, port: Int): SocketChannel

  protected def configureServerChannel(socketChannel: SocketChannel, recvBufferSize: Int, sendBufferSize: Int) {
    socketChannel.configureBlocking(false)
    socketChannel.socket().setTcpNoDelay(true)
    socketChannel.socket().setSendBufferSize(sendBufferSize)

    debug("Accepted connection from %s on %s. sendBufferSize [actual|requested]: [%d|%d] recvBufferSize [actual|requested]: [%d|%d]"
            .format(socketChannel.socket.getInetAddress, socketChannel.socket.getLocalSocketAddress,
                    socketChannel.socket.getSendBufferSize, sendBufferSize,
                    socketChannel.socket.getReceiveBufferSize, recvBufferSize))
  }

  protected def configureClientChannel(channel: SocketChannel, readBufferSize: Int, writeBufferSize: Int, readTimeoutMs: Int) = {
    info("Configuring plaintext client channel")
    if (readBufferSize > 0)
      channel.socket.setReceiveBufferSize(readBufferSize)
    if (writeBufferSize > 0)
      channel.socket.setSendBufferSize(writeBufferSize)
    channel.configureBlocking(true)
    channel.socket.setSoTimeout(readTimeoutMs)
    channel.socket.setKeepAlive(true)
    channel.socket.setTcpNoDelay(true)
  }
}
