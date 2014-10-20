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

package kafka.network.ssl

import java.nio.channels.{SocketChannel, ServerSocketChannel}

import kafka.network.{ChannelFactory, KafkaChannel}

object SSLChannelFactory extends ChannelFactory {
  private val config = SSLConnectionConfig.server
  SSLAuth.initialize(config)
  
  protected def createServerChannelImpl(serverSocketChannel: ServerSocketChannel): KafkaChannel = {
    SSLSocketChannel.makeSecureServerConnection(serverSocketChannel.accept(),
                                                config.wantClientAuth,
                                                config.needClientAuth)
  }

  protected def createClientChannelImpl(host: String, port: Int): SocketChannel = {
    SSLSocketChannel.makeSecureClientConnection(SocketChannel.open(), host, port)
  }

  override protected def configureClientChannel(channel: SocketChannel, readBufferSize: Int, writeBufferSize: Int, readTimeoutMs: Int): Unit = {
    val secureChannel = channel.asInstanceOf[SSLSocketChannel]
    if (readBufferSize > 0)
      secureChannel.socket.setReceiveBufferSize(readBufferSize)
    if (writeBufferSize > 0)
      secureChannel.socket.setSendBufferSize(writeBufferSize)
    secureChannel.simulateBlocking(true)
    secureChannel.socket.setSoTimeout(readTimeoutMs)
    secureChannel.socket.setKeepAlive(true)
    secureChannel.socket.setTcpNoDelay(true)
  }
}
