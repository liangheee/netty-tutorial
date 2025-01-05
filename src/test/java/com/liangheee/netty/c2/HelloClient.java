package com.liangheee.netty.c2;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;

public class HelloClient {
    public static void main(String[] args) throws InterruptedException {
        // 1. 创建客户端启动器，组装Netty组件
        new Bootstrap()
                // 2.创建boss和worker组
                .group(new NioEventLoopGroup())
                // 3. 指定客户端SocketChannel类型
                .channel(NioSocketChannel.class)
                // 4. 指定channel处理器
                .handler(
                        // 5. 对channel进行初始化，通过泛型指定客户端和服务端通信的Channel类型
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline().addLast(new StringEncoder()); // 指定编码器，将字符串编码为ByteBuf
                            }
                        }
                )
                // 6. 连接服务端
                .connect(new InetSocketAddress("localhost",8080))
                // 7. 阻塞等待客户端和服务端连接建立
                .sync()
                // 8. 获取客户端和服务端数据传输通道
                .channel()
                // 9. 向服务端写入数据
                .writeAndFlush("hello, world");

    }
}
