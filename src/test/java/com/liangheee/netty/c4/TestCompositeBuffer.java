package com.liangheee.netty.c4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;

import static com.liangheee.netty.c4.TestByteBuf.log;

public class TestCompositeBuffer {
    public static void main(String[] args) {
        ByteBuf f1 = ByteBufAllocator.DEFAULT.buffer(5);
        f1.writeBytes(new byte[]{1,2,3,4,5});
        ByteBuf f2 = ByteBufAllocator.DEFAULT.buffer(5);
        f2.writeBytes(new byte[]{6,7,8,9,10});

        CompositeByteBuf compositeByteBuf = ByteBufAllocator.DEFAULT.compositeBuffer();
        // 如果设置increaseWriterIndex参数为true，默认是不会对write指针做增长的
        compositeByteBuf.addComponents(true,f1, f2);
        compositeByteBuf.retain();
        compositeByteBuf.writeBytes(new byte[]{11,12,13,14,15});
        log(compositeByteBuf);
    }
}
