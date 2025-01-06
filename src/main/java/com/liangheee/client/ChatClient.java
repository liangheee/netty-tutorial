package com.liangheee.client;

import com.liangheee.message.*;
import com.liangheee.protocol.MessageSharableCodec;
import com.liangheee.protocol.ProtocolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ChatClient {
    private static LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
    private static MessageSharableCodec MESSAGE_CODEC = new MessageSharableCodec();
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            ChannelFuture channelFuture = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ProtocolFrameDecoder()); // 解决黏包和半包问题
                            ch.pipeline().addLast(MESSAGE_CODEC);
                            // 检测写空闲状态
                            ch.pipeline().addLast(new IdleStateHandler(0,3,0));
                            ch.pipeline().addLast(new ChannelDuplexHandler(){
                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    if(evt instanceof IdleStateEvent){
                                        IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                                        if(idleStateEvent.state() == IdleState.WRITER_IDLE){
                                            // 写空闲后，向服务端发送心跳包
                                            log.debug("发送心跳包");
                                            ctx.writeAndFlush(new PingMessage());
                                        }
                                    }
                                }
                            });
                            ch.pipeline().addLast(channelInboundHandlerAdapter());
                        }
                    })
                    .connect(new InetSocketAddress("localhost", 8080));
            Channel channel = channelFuture.sync().channel();
            channel.closeFuture().sync();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    private static ChannelInboundHandlerAdapter channelInboundHandlerAdapter() {
        CountDownLatch WAIT_LOGIN = new CountDownLatch(1);
        AtomicBoolean LOGIN_SUCCESS = new AtomicBoolean(false);
        AtomicBoolean EXIT = new AtomicBoolean(false);
        return new ChannelInboundHandlerAdapter() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                // 连接建立时触发active事件
                new Thread(() -> {
                    try (Scanner scanner = new Scanner(System.in)) {
                        System.out.println("请输入登陆账号：");
                        String username = scanner.nextLine();
                        System.out.println("请输入登陆密码：");
                        String password = scanner.nextLine();
                        LoginRequestMessage loginRequestMessage = new LoginRequestMessage(username, password);
                        ctx.writeAndFlush(loginRequestMessage);

                        // 等待登陆响应
                        WAIT_LOGIN.await();

                        // 登陆失败
                        if (!LOGIN_SUCCESS.get()) {
                            // 关闭channel
                            ctx.channel().close();
                            return;
                        }

                        // 登陆成功
                        while (true) {
                            System.out.println("==================================");
                            System.out.println("send [username] [content]");
                            System.out.println("gsend [group name] [content]");
                            System.out.println("gcreate [group name] [m1,m2,m3...]");
                            System.out.println("gmembers [group name]");
                            System.out.println("gjoin [group name]");
                            System.out.println("gquit [group name]");
                            System.out.println("quit");
                            System.out.println("==================================");
                            String command = null;
                            command = scanner.nextLine();
                            String[] commands = command.split(" ");
                            switch (commands[0]){
                                case "send":
                                    ChatRequestMessage chatRequestMessage = new ChatRequestMessage(username, commands[1], commands[2]);
                                    ctx.writeAndFlush(chatRequestMessage);
                                    break;
                                case "gsend":
                                    GroupChatRequestMessage groupChatRequestMessage = new GroupChatRequestMessage(username, commands[1], commands[2]);
                                    ctx.writeAndFlush(groupChatRequestMessage);
                                    break;
                                case "gcreate":
                                    Set<String> members = new HashSet<>(Arrays.asList(commands[2].split(",")));
                                    members.add(username);
                                    GroupCreateRequestMessage groupCreateRequestMessage = new GroupCreateRequestMessage(username,commands[1],new HashSet<>(members));
                                    ctx.writeAndFlush(groupCreateRequestMessage);
                                    break;
                                case "gmembers":
                                    GroupMembersRequestMessage groupMembersRequestMessage = new GroupMembersRequestMessage(commands[1]);
                                    ctx.writeAndFlush(groupMembersRequestMessage);
                                    break;
                                case "gjoin":
                                    GroupJoinRequestMessage groupJoinRequestMessage = new GroupJoinRequestMessage(username, commands[1]);
                                    ctx.writeAndFlush(groupJoinRequestMessage);
                                    break;
                                case "gquit":
                                    GroupQuitRequestMessage groupQuitRequestMessage = new GroupQuitRequestMessage(username, commands[1]);
                                    ctx.writeAndFlush(groupQuitRequestMessage);
                                    break;
                                case "quit":
                                    ctx.channel().close();
                                    break;
                            }

                            if(EXIT.get()){
                                return;
                            }
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }, "system-in").start();
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                log.debug("{}",msg);
                if (msg instanceof LoginResponseMessage) {
                    LoginResponseMessage loginResponseMessage = (LoginResponseMessage) msg;
                    if (loginResponseMessage.isSuccess()) {
                        LOGIN_SUCCESS.compareAndSet(false, true);
                    }
                    WAIT_LOGIN.countDown();
                }
            }

            @Override
            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                log.debug("连接已经断开，按任意键退出..");
                EXIT.set(true);
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                log.debug("连接已经断开，按任意键退出..{}", cause.getMessage());
                EXIT.set(true);
            }
        };
    }
}
