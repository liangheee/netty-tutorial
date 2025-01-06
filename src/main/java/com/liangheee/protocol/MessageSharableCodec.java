package com.liangheee.protocol;

import com.liangheee.config.Config;
import com.liangheee.message.Message;
import com.liangheee.protocol.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * 必须配合LengthFieldBasedFrameDecoder(1024,12,4,0,0)
 */
@Slf4j
@ChannelHandler.Sharable
public class MessageSharableCodec extends MessageToMessageCodec<ByteBuf,Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) throws Exception {
        ByteBuf out = ctx.alloc().buffer();
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
        // ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // ObjectOutputStream oos = new ObjectOutputStream(bos);
        // oos.writeObject(msg);
        // byte[] byteArray = bos.toByteArray();
        byte[] byteArray = algorithm.serialize(msg);

        // 4字节正文长度
        out.writeInt(byteArray.length);
        // 消息正文
        out.writeBytes(byteArray);

        outList.add(out);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 魔数
        byte[] magicNum = new byte[4];
        in.readBytes(magicNum,0,4);
        // 版本号
        byte version = in.readByte();
        // 序列化算法
        byte serializeType = in.readByte();
        // 指令类型
        byte commandType = in.readByte();
        // 指令序号
        int messageSequenceId = in.readInt();
        // 对齐填充
        byte alignFilled = in.readByte();
        // 正文长度
        int length = in.readInt();

        log.debug("{},{},{},{},{},{}",magicNum,version,serializeType,commandType,messageSequenceId,alignFilled);

        // 正文内容
        byte[] bytes = new byte[length];
        in.readBytes(bytes,0,length);
        // ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        // Message msg = (Message) ois.readObject();
        Serializer.Algorithm serDe = Serializer.Algorithm.values()[serializeType];
        Message msg = serDe.deSerialize(Message.getMessageClass(commandType), bytes);

        out.add(msg);
    }
}
