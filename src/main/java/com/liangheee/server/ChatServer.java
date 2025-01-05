package com.liangheee.server;

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
                            ch.pipeline().addLast(LOGIN_REQUEST_HANDLER);
                            ch.pipeline().addLast(CHAT_REQUEST_HANDLER);
                            ch.pipeline().addLast(GROUP_CREATE_REQUEST_HANDLER);
                            ch.pipeline().addLast(GROUP_CHAT_REQUEST_HANDLER);
                            ch.pipeline().addLast(GROUP_JOIN_REQUEST_HANDLER);
                            ch.pipeline().addLast(GROUP_QUIT_REQUEST_HANDLER);
                            ch.pipeline().addLast(GROUP_MEMBERS_REQUEST_HANDLER);
                        }
                    }).bind(8080);

            Channel channel = channelFuture.channel();
            channel.closeFuture().sync();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
