package com.liangheee.netty.c2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

public class HelloServer {
    public static void main(String[] args) {
        // 1.创建一个服务端启动器，组装Netty组件
        new ServerBootstrap()
                // 2.创建封装了BossEventLoop和WorkerEventLoop的组，EventLoop可以理解为selector + thread的组合
                // 类比NIO中案例，Boss用于处理accept事件，worker用于处理read、write事件等
                .group(new NioEventLoopGroup())
                // 3.指定服务端SocketChannel类型
                .channel(NioServerSocketChannel.class)
                // 4.指定worker(child)处理器，用于处理channel中的数据
                .childHandler(
                        // 5. channel数据传输通道初始化，泛型指定服务端和客户端通信的channel类型为SocketChannel
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                // 6. 往处理流水线中添加一系列处理器
                                socketChannel.pipeline().addLast(new StringDecoder()); // 用于ByteBuf解码，也就是将字节数组转化为字符串
                                socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){ // 用于处理读入数据
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        System.out.println(msg);
                                    }
                                });
                            }
                        }
                )
                // 7. 指定服务端端口
                .bind(8080);
    }
}
