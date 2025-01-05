package com.liangheee.netty.c3;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Scanner;

@Slf4j
public class CloseFutureClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Channel channel = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        // LoggingHandler是Netty提供的一个用于打印Netty执行流程的一个日志处理器
                        nioSocketChannel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        nioSocketChannel.pipeline().addLast(new StringEncoder());
                    }
                })
                .connect(new InetSocketAddress("localhost", 8080))
                .sync()
                .channel();

        // 写入数据线程
        new Thread(() -> {
              try (Scanner scanner = new Scanner(System.in)) {
                  while (true) {
                      String line = scanner.nextLine();
                      if ("q".equals(line)) {
                          channel.close();
                          break;
                      } else {
                          channel.writeAndFlush(line);
                      }
                  }
              }
        },"scanner-thread").start();

        ChannelFuture channelFuture = channel.closeFuture();

        // main线程 同步关闭
        // channelFuture.sync();

        // nio线程 异步关闭
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                log.debug("关闭后处理操作");
                // 优雅的关闭NioEventGroup，group会暂停接受新的事件，但会对已有的事件和正在处理中的事件处理完成后才会关闭
                group.shutdownGracefully();
            }
        });

        log.debug("end");
    }
}
