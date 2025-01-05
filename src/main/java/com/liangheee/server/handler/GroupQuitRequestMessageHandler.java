package com.liangheee.server.handler;

import com.liangheee.message.GroupQuitRequestMessage;
import com.liangheee.message.GroupQuitResponseMessage;
import com.liangheee.server.session.Group;
import com.liangheee.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

@ChannelHandler.Sharable
public class GroupQuitRequestMessageHandler extends SimpleChannelInboundHandler<GroupQuitRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupQuitRequestMessage msg) throws Exception {
        String groupName = msg.getGroupName();
        String username = msg.getUsername();

        Group group = GroupSessionFactory.getGroupSession().removeMember(groupName, username);
        GroupQuitResponseMessage message;
        if(group != null){
            // 群组存在
            message = new GroupQuitResponseMessage(username,"退出群组【" + groupName + "】");
            List<Channel> membersChannel = GroupSessionFactory.getGroupSession().getMembersChannel(groupName);
            membersChannel.forEach(ch -> ch.writeAndFlush(message));
        }else{
            // 群组不存在
            message = new GroupQuitResponseMessage(false,"退出群组失败，群组【" + groupName + "】不存在");
            ctx.writeAndFlush(message);
        }
    }
}
