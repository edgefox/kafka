package org.apache.kafka.common.utils;

import java.util.zip.Checksum;

public class MessageLengthChecksum implements Checksum {
    private long length = 0L;

    public void update(int b) {
        length++;
    }

    public void update(byte[] b, int off, int len) {
        length = len;
    }

    public long getValue() {
        return length;
    }

    public void reset() {
        length = 0;
    }
}