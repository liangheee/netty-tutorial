package com.liangheee.netty.c3;

import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class TestEventLoop {
    public static void main(String[] args) {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2); // io事件、普通任务、定时任务
        // EventLoopGroup eventLoopGroup = new DefaultEventLoop(); // 普通任务、定时任务

        // 获取EventLoop事件循环对象
        System.out.println(eventLoopGroup.next());
        System.out.println(eventLoopGroup.next());
        System.out.println(eventLoopGroup.next());
        System.out.println(eventLoopGroup.next());

        // 执行普通任务
//        eventLoopGroup.next().submit(() -> {
//            log.debug("execute");
//        });

        // 执行定时任务
        eventLoopGroup.next().scheduleAtFixedRate(() -> {
            log.debug("scheduled...");
        },0,2, TimeUnit.SECONDS);

        log.debug("main");
    }
}
