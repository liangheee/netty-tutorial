package com.liangheee.server.handler;

import com.liangheee.message.GroupChatRequestMessage;
import com.liangheee.message.GroupChatResponseMessage;
import com.liangheee.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.List;
import java.util.stream.Collectors;

@ChannelHandler.Sharable
public class GroupChatRequestMessageHandler extends SimpleChannelInboundHandler<GroupChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupChatRequestMessage msg) throws Exception {
        String groupName = msg.getGroupName();
        String content = msg.getContent();
        String sender = msg.getFrom();

        boolean groupExists = GroupSessionFactory.getGroupSession().groupExists(groupName);

        GroupChatResponseMessage groupChatResponseMessage;
        if(!groupExists){
            // 如果群组不存在
            groupChatResponseMessage = new GroupChatResponseMessage(false,"发送消息失败，群组【" + groupName + "】不存在");
            groupChatResponseMessage.setSequenceId(msg.getSequenceId());
            ctx.writeAndFlush(groupChatResponseMessage);
        }else{
            // 如果群组存在
            List<Channel> membersChannel = GroupSessionFactory.getGroupSession().getMembersChannel(groupName);
            Channel channel = ctx.channel();
            // 过滤发送群消息的用户channel
            List<Channel> membersChannelExclusiveSender = membersChannel.stream().filter(ch -> ch != channel).collect(Collectors.toList());

            groupChatResponseMessage = new GroupChatResponseMessage(sender, content);
            groupChatResponseMessage.setSequenceId(msg.getSequenceId());

            membersChannelExclusiveSender.forEach(ch -> ch.writeAndFlush(groupChatResponseMessage));
        }

    }
}
