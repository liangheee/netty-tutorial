package com.liangheee.server.handler;

import com.liangheee.message.PingMessage;
import com.liangheee.message.PongMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class PingMessageHandler extends SimpleChannelInboundHandler<PingMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PingMessage msg) throws Exception {
        PongMessage pongMessage = new PongMessage();
        pongMessage.setSequenceId(msg.getSequenceId());
        ctx.writeAndFlush(msg);
    }
}
