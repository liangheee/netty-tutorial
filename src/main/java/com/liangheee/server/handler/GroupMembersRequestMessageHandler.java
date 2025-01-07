package com.liangheee.server.handler;

import com.liangheee.message.GroupMembersRequestMessage;
import com.liangheee.message.GroupMembersResponseMessage;
import com.liangheee.server.session.GroupSessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Set;

@ChannelHandler.Sharable
public class GroupMembersRequestMessageHandler extends SimpleChannelInboundHandler<GroupMembersRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupMembersRequestMessage msg) throws Exception {
        String groupName = msg.getGroupName();
        boolean groupExists = GroupSessionFactory.getGroupSession().groupExists(groupName);

        GroupMembersResponseMessage message;
        if(!groupExists){
            message = new GroupMembersResponseMessage(false,"获取群组成员失败，群组【" + groupName + "】不存在");
        }else{
            Set<String> members = GroupSessionFactory.getGroupSession().getMembers(groupName);
            message = new GroupMembersResponseMessage(members);
        }
        message.setSequenceId(msg.getSequenceId());
        ctx.writeAndFlush(message);
    }
}
