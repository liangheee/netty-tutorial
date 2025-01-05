package com.liangheee.nio.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.liangheee.nio.c1.ByteBufferUtil.debugAll;

@Slf4j
public class MultiThreadServer {
    public static void main(String[] args) throws IOException {
        Thread.currentThread().setName("boss");
        Selector selector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT,null);
        ssc.bind(new InetSocketAddress(8080));
        log.debug("scc: {}",ssc);
        Worker[] workers = new Worker[2];
        for(int i = 0;i < 2;i++){
            workers[i] = new Worker("worker-" + i);
        }
        int count = 0;
        while(true){
            selector.select();
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while(iter.hasNext()){
                SelectionKey key = iter.next();
                iter.remove();
                if(key.isAcceptable()){
                    SocketChannel sc = ssc.accept();
                    log.debug("before connected...{}",sc);
                    sc.configureBlocking(false);
                    workers[(count++) % workers.length].register(sc);
                    log.debug("after connected...{}",sc);
                }
            }
        }
    }

    static class Worker implements Runnable {
        private String name;
        private Thread thread;
        private Selector selector;
        private volatile boolean start;
        private final ConcurrentLinkedQueue<Runnable> tasks = new ConcurrentLinkedQueue<>();


        public Worker(String name) {
            this.name = name;
        }

        public void register(SocketChannel sc){
            if(!start){
                try {
                    selector = Selector.open();
                    thread = new Thread(this,name);
                    thread.start();
                    start = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            tasks.offer(() -> {
                try {
                    ByteBuffer buffer = ByteBuffer.allocate(16);
                    sc.register(selector,SelectionKey.OP_READ,buffer);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            });
            selector.wakeup();
        }

        @Override
        public void run() {
            while(true){
                try {
                    selector.select();
                    Runnable task = tasks.poll();
                    if(task != null){
                        task.run();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while(iter.hasNext()){
                    SelectionKey key = iter.next();
                    iter.remove();
                    try {
                        if(key.isReadable()){
                            SocketChannel sc = (SocketChannel) key.channel();
                            ByteBuffer buffer = (ByteBuffer) key.attachment();
                            int read = sc.read(buffer);
                            if(read == -1){
                                // 处理客户端正常关闭触发的读事件
                                key.cancel();
                            }else{
                                log.debug("before read...{}",sc);
                                // 对读取到的数据进行分拆，解决黏包和半包问题
                                split(buffer);
                                // 扩容处理半包问题
                                if(buffer.position() == buffer.limit()){
                                    ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                                    buffer.flip();
                                    newBuffer.put(buffer);
                                    key.attach(newBuffer);
                                }
                                log.debug("after read...{}",sc);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // 处理客户端异常关闭触发的读事件
                        key.cancel();
                    }
                }
            }
        }

        public void split(ByteBuffer source){
            source.flip();
            for(int i = 0;i < source.limit();i++){
                if(source.get(i) == '\n'){
                    int length = i + 1 - source.position();
                    ByteBuffer target = ByteBuffer.allocate(length);
                    for(int j = 0;j < length;j++){
                        target.put(source.get());
                    }
                    target.flip();
                    debugAll(target);
                }
            }
            source.compact();
        }
    }
}
