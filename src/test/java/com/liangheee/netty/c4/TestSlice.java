package com.liangheee.netty.c4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import static com.liangheee.netty.c4.TestByteBuf.log;

public class TestSlice {
    public static void main(String[] args) {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(10);
        buffer.writeBytes(new byte[]{'a','b','c','d','e','f','g','h','i','j'});
        log(buffer);

        ByteBuf f1 = buffer.slice(0, 5);
//        f1.retain();
        ByteBuf f2 = buffer.slice(5, 5);
        f2.retain();

        log(f1);
        log(f2);

        buffer.release();
        log(f1);
        log(f2);

    }
}
