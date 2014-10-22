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

package kafka.api

import java.nio.ByteBuffer

import kafka.api.ApiUtils._
import kafka.common.ErrorMapping
import kafka.network.{ChannelType, BoundedByteBufferSend, RequestChannel}
import kafka.network.RequestChannel.Response
import kafka.utils.Logging

import scala.collection.mutable.ListBuffer

object TopicMetadataForChannelRequest extends Logging {
  val CurrentVersion = 0.shortValue
  val DefaultClientId = ""

  /**
   * TopicMetadataWithChannelsRequest has the following format -
   * number of topics (4 bytes) list of topics (2 bytes + topic.length per topic) detailedMetadata (2 bytes) timestamp (8 bytes) count (4 bytes)
   */

  def readFrom(buffer: ByteBuffer): TopicMetadataForChannelRequest = {
    val versionId = buffer.getShort
    val correlationId = buffer.getInt
    val clientId = readShortString(buffer)
    val numTopics = readIntInRange(buffer, "number of topics", (0, Int.MaxValue))
    val topics = new ListBuffer[String]()
    for (i <- 0 until numTopics)
      topics += readShortString(buffer)
    val channelType = ChannelType.getChannelType(readShortString(buffer))
    new TopicMetadataForChannelRequest(versionId, correlationId, clientId, topics.toList, channelType)
  }
}

case class TopicMetadataForChannelRequest(val versionId: Short,
                                          val correlationId: Int,
                                          val clientId: String,
                                          val topics: Seq[String],
                                          val channelType: ChannelType)
  extends RequestOrResponse(Some(RequestKeys.MetadataForChannelsKey)) {
  
  def this(request: TopicMetadataRequest, channelType: ChannelType) = {
    this(request.versionId, request.correlationId, request.clientId, request.topics, channelType)
  }

  def this(topics: Seq[String], correlationId: Int, channelType: ChannelType) =
    this(TopicMetadataForChannelRequest.CurrentVersion, correlationId, TopicMetadataForChannelRequest.DefaultClientId, topics, channelType)

  def writeTo(buffer: ByteBuffer) {
    buffer.putShort(versionId)
    buffer.putInt(correlationId)
    writeShortString(buffer, clientId)
    buffer.putInt(topics.size)
    topics.foreach(topic => writeShortString(buffer, topic))
    writeShortString(buffer, channelType.name)
  }

  def sizeInBytes(): Int = {
    2 + /* version id */
    4 + /* correlation id */
    shortStringLength(clientId) + /* client id */
    4 + /* number of topics */
    topics.foldLeft(0)(_ + shortStringLength(_)) + /* topics */
    shortStringLength(channelType.name)
  }

  override def toString(): String = {
    describe(true)
  }

  override def handleError(e: Throwable, requestChannel: RequestChannel, request: RequestChannel.Request): Unit = {
    val topicMetadata = topics.map {
                                     topic => TopicMetadata(topic, Nil, ErrorMapping.codeFor(e.getClass.asInstanceOf[Class[Throwable]]))
                                   }
    val errorResponse = TopicMetadataResponse(Seq(), topicMetadata, correlationId)
    requestChannel.sendResponse(new Response(request, new BoundedByteBufferSend(errorResponse)))
  }

  override def describe(details: Boolean): String = {
    val TopicMetadataWithChannelsRequest = new StringBuilder
    TopicMetadataWithChannelsRequest.append("Name: " + this.getClass.getSimpleName)
    TopicMetadataWithChannelsRequest.append("; Version: " + versionId)
    TopicMetadataWithChannelsRequest.append("; CorrelationId: " + correlationId)
    TopicMetadataWithChannelsRequest.append("; ClientId: " + clientId)
    if (details)
      TopicMetadataWithChannelsRequest.append("; Topics: " + topics.mkString(","))
    TopicMetadataWithChannelsRequest.append("; ChannelType: " + channelType.name)
    TopicMetadataWithChannelsRequest.toString()
  }
}