package com.liangheee.protocol;

import com.liangheee.message.LoginRequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class TestMessageCodec {
    public static void main(String[] args) throws Exception {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                new LoggingHandler(LogLevel.DEBUG),
//                new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0),
                new MessageCodec()
        );
        LoginRequestMessage message = new LoginRequestMessage("zhangsan", "123");

//        embeddedChannel.writeOutbound(message);

        ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null,message,out);

//        embeddedChannel.writeInbound(out);

        // 测试半包，报错 java.lang.IndexOutOfBoundsException: readerIndex(16) + length(206) exceeds writerIndex(100)
        ByteBuf s1 = out.slice(0, 100);
        ByteBuf s2 = out.slice(100, out.readableBytes() - 100);
        s1.retain(); // 引用计数 2
        embeddedChannel.writeInbound(s1); // release 1
        embeddedChannel.writeInbound(s2);

        embeddedChannel.writeInbound(out);
    }
}
