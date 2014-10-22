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

import java.nio.ByteBuffer

import kafka.api.ApiUtils._
import kafka.common.{BrokerChannelNotAvailableException, KafkaException, BrokerNotAvailableException}
import kafka.utils.Json
import kafka.utils.Utils._

object ChannelInfo {

  def createChannelInfo(brokerId: Int, channelInfoString: String): ChannelInfo = {
    if(channelInfoString == null)
      throw new BrokerNotAvailableException("Channel for broker id %s does not exist".format(brokerId))
    try {
      Json.parseFull(channelInfoString) match {
        case Some(m) =>
          val channelInfo = m.asInstanceOf[Map[String, Any]]
          val brokerId = channelInfo.get("brokerId").get.asInstanceOf[Int]
          val port = channelInfo.get("port").get.asInstanceOf[Int]
          val channelType = ChannelType.getChannelType(channelInfo.get("type").get.asInstanceOf[String])
          new ChannelInfo(brokerId, port, channelType)
        case None =>
          throw new BrokerChannelNotAvailableException("Broker id %d does not exist".format(brokerId))
      }
    } catch {
      case t: Throwable => throw new KafkaException("Failed to parse the broker channel info from zookeeper: " + channelInfoString, t)
    }
  }

  def readFrom(buffer: ByteBuffer): ChannelInfo = {
    val brokerId = buffer.getInt
    val port = buffer.getInt
    val channelType = readShortString(buffer)
    new ChannelInfo(brokerId, port, ChannelType.getChannelType(channelType))
  }
}

class ChannelInfo(val brokerId: Int, val port: Int, val channelType: ChannelType) {

  def writeTo(buffer: ByteBuffer) {
    buffer.putInt(brokerId)
    buffer.putInt(port)
    writeShortString(buffer, channelType.name)
  }

  def sizeInBytes: Int = shortStringLength(channelType.name) /* channel type */ + 4 /* port */ + 4 /* broker id*/

  override def toString: String = "brokerId:%d,port:%d,type:%s".format(brokerId, port, channelType)

  override def equals(obj: Any): Boolean = {
    obj match {
      case null => false
      case n: ChannelInfo => brokerId == n.brokerId && port == n.port && channelType == n.channelType
      case _ => false
    }
  }

  override def hashCode(): Int = hashcode(brokerId, port, channelType)
}
