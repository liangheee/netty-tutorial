package com.liangheee.server;

import com.liangheee.config.Config;
import com.liangheee.protocol.MessageSharableCodec;
import com.liangheee.protocol.ProtocolFrameDecoder;
import com.liangheee.server.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatServer {
    public static void main(String[] args) {
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageSharableCodec MESSAGE_CODEC = new MessageSharableCodec();
        LoginRequestMessageHandler LOGIN_REQUEST_HANDLER = new LoginRequestMessageHandler();
        ChatRequestMessageHandler CHAT_REQUEST_HANDLER = new ChatRequestMessageHandler();
        GroupCreateRequestMessageHandler GROUP_CREATE_REQUEST_HANDLER = new GroupCreateRequestMessageHandler();
        GroupChatRequestMessageHandler GROUP_CHAT_REQUEST_HANDLER = new GroupChatRequestMessageHandler();
        GroupJoinRequestMessageHandler GROUP_JOIN_REQUEST_HANDLER = new GroupJoinRequestMessageHandler();
        GroupQuitRequestMessageHandler GROUP_QUIT_REQUEST_HANDLER = new GroupQuitRequestMessageHandler();
        GroupMembersRequestMessageHandler GROUP_MEMBERS_REQUEST_HANDLER = new GroupMembersRequestMessageHandler();
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            ChannelFuture channelFuture = new ServerBootstrap()
                    .group(group)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ProtocolFrameDecoder()); // 解决黏包和半包问题
                            ch.pipeline().addLast(LOGGING_HANDLER);
                            ch.pipeline().addLast(MESSAGE_CODEC);
                            // 为了处理假死状态，加入空闲状态处理器，检测读空闲 或 写空闲
                            // 对于Server一般检测读空闲，这里我们设置读空闲超过5s就触发读空闲事件处理
                            ch.pipeline().addLast(new IdleStateHandler(5,0,0));
                            ch.pipeline().addLast(new ChannelDuplexHandler(){
                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    if(evt instanceof IdleStateEvent){
                                        IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                                        if(idleStateEvent.state() == IdleState.READER_IDLE){
                                            // 处理读空闲事件
                                            log.debug("已经5s没有读到数据");
                                            ctx.channel().close();
                                        }
                                    }
                                }
                            });
                            ch.pipeline().addLast(LOGIN_REQUEST_HANDLER);
                            ch.pipeline().addLast(CHAT_REQUEST_HANDLER);
                            ch.pipeline().addLast(GROUP_CREATE_REQUEST_HANDLER);
                            ch.pipeline().addLast(GROUP_CHAT_REQUEST_HANDLER);
                            ch.pipeline().addLast(GROUP_JOIN_REQUEST_HANDLER);
                            ch.pipeline().addLast(GROUP_QUIT_REQUEST_HANDLER);
                            ch.pipeline().addLast(GROUP_MEMBERS_REQUEST_HANDLER);
                        }
                    }).bind(Config.serverPort());

            Channel channel = channelFuture.channel();
            channel.closeFuture().sync();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
