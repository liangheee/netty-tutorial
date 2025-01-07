package com.liangheee.client.handler;

import com.liangheee.message.RpcResponseMessage;
import com.liangheee.rpc.PromiseContainer;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class RpcResponseMessageHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) throws Exception {
        int sequenceId = msg.getSequenceId();
        // promise使用后立马清理，避免冷数据常驻内存
        Promise<Object> promise = PromiseContainer.remove(sequenceId);
        if(promise != null){
            Object returnValue = msg.getReturnValue();
            Exception exceptionValue = msg.getExceptionValue();
            if(exceptionValue == null){
                // 远程调用成功
                promise.setSuccess(returnValue);
            } else {
                // 远程调用失败
                promise.setFailure(exceptionValue);
            }
        }
    }
}
