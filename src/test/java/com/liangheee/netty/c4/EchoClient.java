package com.liangheee.netty.c4;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Slf4j
public class EchoClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Channel channel = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // StringEncoder是一个outbound处理器，那么最后会将客户端发送的ByteBuf发往head进行release
                        ch.pipeline().addLast(new StringEncoder());
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                System.out.println(buf.toString(StandardCharsets.UTF_8));
                                // 传递buf给tail handler关闭
                                super.channelRead(ctx, msg);
                            }
                        });
                    }
                }).connect(new InetSocketAddress("localhost", 8080))
                .sync()
                .channel();

        new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while(true){
                    String line = scanner.nextLine();
                    if("q".equals(line)){
                        channel.close();
                        break;
                    }else{
                        channel.writeAndFlush(line);
                    }
                }
            }
        },"scanner-thread").start();

        ChannelFuture channelFuture = channel.closeFuture();
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                log.debug("客户端关闭后续处理操作...");
                group.shutdownGracefully();
            }
        });
    }
}
