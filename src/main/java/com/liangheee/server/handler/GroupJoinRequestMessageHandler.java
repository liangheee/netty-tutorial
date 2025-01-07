package com.liangheee.server.handler;

import com.liangheee.message.GroupJoinRequestMessage;
import com.liangheee.message.GroupJoinResponseMessage;
import com.liangheee.server.session.Group;
import com.liangheee.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

@ChannelHandler.Sharable
public class GroupJoinRequestMessageHandler extends SimpleChannelInboundHandler<GroupJoinRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupJoinRequestMessage msg) throws Exception {
        String groupName = msg.getGroupName();
        String username = msg.getUsername();

        Group group = GroupSessionFactory.getGroupSession().joinMember(groupName, username);
        GroupJoinResponseMessage message;
        if(group != null){
            // 当前组存在，加入组成功
            message = new GroupJoinResponseMessage(true,username + "加入群组【" + groupName + "】");
            message.setSequenceId(msg.getSequenceId());
            List<Channel> membersChannel = GroupSessionFactory.getGroupSession().getMembersChannel(groupName);
            membersChannel.forEach(ch -> ch.writeAndFlush(message));
        }else{
            message = new GroupJoinResponseMessage(false,"群组【" + groupName + "】可能存不在，加入群组失败");
            message.setSequenceId(msg.getSequenceId());
            ctx.writeAndFlush(message);
        }
    }
}
