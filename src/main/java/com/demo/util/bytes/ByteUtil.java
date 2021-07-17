package com.demo.util.bytes;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ByteUtil {

    public static byte[] covertLongToByte(long num) throws Exception {
        ByteBuf byteBuf = Unpooled.compositeBuffer();
        byteBuf.writeLong(num);
        if (byteBuf.hasArray()) {
            return byteBuf.array();
        } else {
            throw new Exception("缓冲出现异常！！！");
        }
    }

    public static long covertByteToLong(byte[] bytes) throws Exception {
        ByteBuf byteBuf = Unpooled.compositeBuffer();
        byteBuf.writeBytes(bytes);
        return byteBuf.getLong(0);
    }
}
