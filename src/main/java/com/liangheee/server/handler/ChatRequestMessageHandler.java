package com.liangheee.server.handler;

import com.liangheee.message.ChatRequestMessage;
import com.liangheee.message.ChatResponseMessage;
import com.liangheee.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatRequestMessage msg) throws Exception {
        String sender = msg.getFrom();
        String receiver = msg.getTo();
        String content = msg.getContent();
        Channel channel = SessionFactory.getSession().getChannel(receiver);
        ChatResponseMessage chatResponseMessage = new ChatResponseMessage(sender, content);
        chatResponseMessage.setSequenceId(msg.getSequenceId());
        channel.writeAndFlush(chatResponseMessage);
    }
}
