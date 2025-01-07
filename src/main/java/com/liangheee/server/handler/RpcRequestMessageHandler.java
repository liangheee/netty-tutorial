package com.liangheee.server.handler;

import com.liangheee.message.RpcRequestMessage;
import com.liangheee.message.RpcResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage msg) throws Exception {
        RpcResponseMessage response = new RpcResponseMessage();
        // 一定要绑定响应消息sequenceId
        response.setSequenceId(msg.getSequenceId());
        try {
            Class<?> clazz = Class.forName(msg.getInterfaceName());
            Object obj  = clazz.newInstance();
            Method method = clazz.getMethod(msg.getMethodName(), msg.getParameterTypes());
            Object invoke = method.invoke(obj, msg.getParameterValue());
            response.setReturnValue(invoke);
        } catch (Exception e){
            e.printStackTrace();
            response.setExceptionValue(new Exception("远程调用出错" + e.getCause().getMessage()));
        }
        ctx.writeAndFlush(response);
    }
}
