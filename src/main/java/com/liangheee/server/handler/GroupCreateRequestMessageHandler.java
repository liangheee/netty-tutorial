package com.liangheee.server.handler;

import com.liangheee.message.GroupCreateRequestMessage;
import com.liangheee.message.GroupCreateResponseMessage;
import com.liangheee.server.session.Group;
import com.liangheee.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;
import java.util.Set;

@ChannelHandler.Sharable
public class GroupCreateRequestMessageHandler extends SimpleChannelInboundHandler<GroupCreateRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupCreateRequestMessage msg) throws Exception {
        String from = msg.getFrom();
        String groupName = msg.getGroupName();
        Set<String> members = msg.getMembers();

        // 创建群组
        Group group = GroupSessionFactory.getGroupSession().createGroup(groupName, members);
        GroupCreateResponseMessage message;
        if(group == null){
            // 创建成功
            message = new GroupCreateResponseMessage(true, from + "创建了群组【" + groupName + "】");
            message.setSequenceId(msg.getSequenceId());
            // 发送创建群聊成功消息
            List<Channel> membersChannel = GroupSessionFactory.getGroupSession().getMembersChannel(groupName);
            membersChannel.forEach(ch -> {
                ch.writeAndFlush(message);
            });
        }else{
            // 创建失败
            message = new GroupCreateResponseMessage(false,"创建群组失败");
            message.setSequenceId(msg.getSequenceId());
            ctx.writeAndFlush(message);
        }

    }
}
