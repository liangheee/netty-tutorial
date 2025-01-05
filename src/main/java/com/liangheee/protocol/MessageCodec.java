package com.liangheee.protocol;

import com.liangheee.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * 自定义消息通信协议
 *      魔数，用来在第一时间判定是否是无效数据包
 *      版本号，可以支持协议的升级
 *      序列化算法，消息正文到底采用哪种序列化反序列化方式，可以由此扩展，例如：json、protobuf、hessian、jdk
 *      指令类型，是登录、注册、单聊、群聊... 跟业务相关
 *      请求序号，为了双工通信，提供异步能力
 *      正文长度
 *      消息正文
 *
 * 必须配合LengthFieldBasedFrameDecoder(1024,12,4,0,0)
 */
@Slf4j
public class MessageCodec extends ByteToMessageCodec<Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        // 4字节魔数 1, 2, 3, 4
        out.writeBytes(new byte[]{1, 2, 3, 4});
        // 1字节版本号
        out.writeByte(1);
        // 1字节序列化算法 0-json 1-jdk
        out.writeByte(1);
        // 1字节指令类型
        out.writeByte(msg.getMessageType());
        // 4字节指令序号
        out.writeInt(msg.getSequenceId());

        // 1字节对齐填充,固定写入0xff，保证消息头部大小为2的幂次
        out.writeByte(0xff);

        // 序列化消息正文
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        byte[] byteArray = bos.toByteArray();

        // 4字节正文长度
        out.writeInt(byteArray.length);
        // 消息正文
        out.writeBytes(byteArray);
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
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Message msg = (Message) ois.readObject();
        out.add(msg);
    }
}
