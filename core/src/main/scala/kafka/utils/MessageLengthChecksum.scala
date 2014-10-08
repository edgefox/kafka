package kafka.utils

import java.util.zip.Checksum

class MessageLengthChecksum() extends Checksum {
  private var length = 0L

  override def update(b: Int): Unit = length += 1

  override def getValue: Long = length & 0xFFFFFFFL

  override def update(b: Array[Byte], off: Int, len: Int): Unit = length = len

  override def reset(): Unit = 0
}
