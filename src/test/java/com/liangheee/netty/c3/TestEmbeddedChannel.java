package com.liangheee.netty.c3;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestEmbeddedChannel {
    public static void main(String[] args) {
        // Netty提供的用于调试handler的类，可以模拟入站和出站操作，可以避免重复写服务端和客户端代码快速校验handler处理结果
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                log.debug("1");
                super.channelRead(ctx,msg);
            }
        });

        channel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                log.debug("2");
                super.channelRead(ctx,msg);
            }
        });

        channel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                log.debug("3");
                super.channelRead(ctx,msg);
            }
        });

        channel.pipeline().addLast(new ChannelOutboundHandlerAdapter(){
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                log.debug("4");
                super.write(ctx, msg, promise);
            }
        });

        channel.pipeline().addLast(new ChannelOutboundHandlerAdapter(){
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                log.debug("5");
                super.write(ctx, msg, promise);
            }
        });

        channel.pipeline().addLast(new ChannelOutboundHandlerAdapter(){
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                log.debug("6");
                super.write(ctx, msg, promise);
            }
        });

//        channel.writeInbound("hello");
        channel.writeOneOutbound("hello,world");
    }
}
