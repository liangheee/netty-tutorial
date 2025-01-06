package com.liangheee.protocol.serialize;

import com.liangheee.config.Config;
import com.liangheee.message.LoginRequestMessage;
import com.liangheee.message.Message;
import com.liangheee.protocol.MessageCodec;
import com.liangheee.protocol.MessageSharableCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class TestSerializer {
    public static void main(String[] args) {
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageSharableCodec MESSAGE_CODEC = new MessageSharableCodec();

        EmbeddedChannel channel = new EmbeddedChannel(
                LOGGING_HANDLER,
                MESSAGE_CODEC,
                LOGGING_HANDLER
        );

        // 测试出站
         LoginRequestMessage message = new LoginRequestMessage("zhangsan", "123");
        // channel.writeOutbound(message);

        // 测试入站
        channel.writeInbound(encode(message));
    }

    public static ByteBuf encode(Message msg){
        ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
        // 4字节魔数 1, 2, 3, 4
        out.writeBytes(new byte[]{1, 2, 3, 4});
        // 1字节版本号
        out.writeByte(1);
        // 1字节序列化算法 0-jdk 1-json
        Serializer.Algorithm algorithm = Config.serDeAlgorithm();
        out.writeByte(algorithm.ordinal());
        // 1字节指令类型
        out.writeByte(msg.getMessageType());
        // 4字节指令序号
        out.writeInt(msg.getSequenceId());

        // 1字节对齐填充,固定写入0xff，保证消息头部大小为2的幂次
        out.writeByte(0xff);

        // 序列化消息正文
        byte[] byteArray = algorithm.serialize(msg);

        // 4字节正文长度
        out.writeInt(byteArray.length);
        // 消息正文
        out.writeBytes(byteArray);

        return out;
    }
}
