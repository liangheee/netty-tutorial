package com.liangheee.server.handler;

import com.liangheee.message.LoginRequestMessage;
import com.liangheee.message.LoginResponseMessage;
import com.liangheee.server.service.UserServiceFactory;
import com.liangheee.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage msg) throws Exception {
        boolean isSuccess = UserServiceFactory.getUserService().login(msg.getUsername(), msg.getPassword());
        LoginResponseMessage message;
        if(isSuccess){
            SessionFactory.getSession().bind(ctx.channel(),msg.getUsername());
            message = new LoginResponseMessage(true, "登陆成功");
        }else{
            message = new LoginResponseMessage(false,"登陆名或密码错误");
        }
        message.setSequenceId(msg.getSequenceId());
        ctx.writeAndFlush(message);
    }
}
