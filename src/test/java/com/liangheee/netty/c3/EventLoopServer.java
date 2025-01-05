package com.liangheee.netty.c3;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

@Slf4j
public class EventLoopServer {
    public static void main(String[] args) {
        DefaultEventLoopGroup defaultEventLoopGroup = new DefaultEventLoopGroup();
        new ServerBootstrap()
                // 细分EventLoopGroup，指定Boss组 -> accept事件  Worker组 -> read事件
                // 第一个参数指定Boss组，通常来说Boss只需要一个线程，处理accept事件。为什么我们不用通过构造参数指定线程数呢？
                //          因为对于一个Server而言，只会创建一个NioServerSocketChannel对象，也就只会在Boss组中创建一个线程用于处理该Channel，那么自然也就不用再指定Boss组的线程数
                // 第二个参数指定Worker组
                .group(new NioEventLoopGroup(),new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        // 如果某一个handler处理耗时较长 -> 拖慢该channel的处理速度 -> 拖住EventGroup中的线程处理该channel中的handler -> 从而影响其他channel的处理速度
                        // 考虑对于处理耗时较长的handler采用专门的EventGroup来处理
                        nioSocketChannel.pipeline().addLast("handler1",new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                log.debug("{}", buf.toString(Charset.defaultCharset()));
                                ctx.fireChannelRead(msg);
                            }
                        });
//                        }).addLast(defaultEventLoopGroup,"handler2",new ChannelInboundHandlerAdapter(){
//                            @Override
//                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                                ByteBuf buf = (ByteBuf) msg;
//                                log.debug("{}",buf.toString(Charset.defaultCharset()));
//                            }
//                        });
                    }
                })
                .bind(8080);
    }
}
