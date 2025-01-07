package com.liangheee.client;

import com.liangheee.client.handler.RpcResponseMessageHandler;
import com.liangheee.config.Config;
import com.liangheee.message.RpcRequestMessage;
import com.liangheee.protocol.MessageSharableCodec;
import com.liangheee.protocol.ProtocolFrameDecoder;
import com.liangheee.rpc.PromiseContainer;
import com.liangheee.server.service.HelloService;
import com.liangheee.server.service.HelloServiceImpl;
import com.liangheee.util.SequenceIdGenerator;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

@Slf4j
public class RpcClientManager {
    private static Channel channel;
    private static final Object LOCK = new Object();

    public static void main(String[] args) {
        HelloService service = getProxyService(HelloServiceImpl.class);
        System.out.println(service.sayHello("zhangsan"));
    }

    public static <T> T getProxyService(Class<T> serviceClass){
        ClassLoader classLoader = serviceClass.getClassLoader();
        Class<?>[] interfaces = serviceClass.getInterfaces();
        // 获取jdk动态代理对象
        Object o = Proxy.newProxyInstance(classLoader, interfaces, (proxy, method, args) -> {
            // 将方法调用转换为rpc请求
            int sequenceId = SequenceIdGenerator.nextId();
            RpcRequestMessage rpcRequestMessage = new RpcRequestMessage(
                    sequenceId,
                    serviceClass.getName(),
                    method.getName(),
                    method.getReturnType(),
                    method.getParameterTypes(),
                    args);
            // 发送rpc请求消息
            getChannel().writeAndFlush(rpcRequestMessage);

            // 采用Nio线程异步等待获取结果
            DefaultPromise<Object> promise = new DefaultPromise<>(getChannel().eventLoop());
            // 放入promise容器中，便于RpcResponseMessageHandler获取(这个handler是在Nio线程中运行的，因此考虑使用Promise进行线程间通信)
            PromiseContainer.put(sequenceId,promise);

            // 等待任务结束，await方法只是等待任务结束，任务失败不会抛出异常，通过isSuccess()方法判断任务是否成功
            promise.await();
            if(promise.isSuccess()){
                // 任务成功，立马获取结果
                return promise.getNow();
            }else{
                // 任务失败，代理对象抛出异常
                throw new RuntimeException(promise.cause());
            }
        });
        return (T) o;
    }

    // channel单例
    public static Channel getChannel(){
        if(channel != null){
            return channel;
        }

        synchronized (LOCK){
            if(channel != null){
                return channel;
            }
            initialChannel();
            return channel;
        }
    }

    private static void initialChannel() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler();
        MessageSharableCodec MESSAGE_CODEC = new MessageSharableCodec();
        RpcResponseMessageHandler RPC_RESPONSE_HANDLER = new RpcResponseMessageHandler();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProtocolFrameDecoder());
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    ch.pipeline().addLast(RPC_RESPONSE_HANDLER);
                }
            });

            channel = bootstrap.connect(new InetSocketAddress("localhost", Config.serverPort())).sync().channel();
            channel.closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    log.debug("channel关闭后的后续操作");
                    group.shutdownGracefully();
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
