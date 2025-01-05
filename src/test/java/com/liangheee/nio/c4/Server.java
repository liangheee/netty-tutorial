package com.liangheee.nio.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import static com.liangheee.nio.c1.ByteBufferUtil.debugRead;

@Slf4j
public class Server {
    private static void split(ByteBuffer source){
        source.flip();
        for(int i = 0;i < source.limit();i++){
            if (source.get(i) == '\n') {
                int length = i + 1 - source.position();
                ByteBuffer target = ByteBuffer.allocate(length);
                for(int j = 0;j < length;j++){
                    target.put(source.get());
                }
                target.flip();
                debugRead(target);
            }
        }
        source.compact();
    }
    public static void main(String[] args) throws IOException {
        // 创建Selector
        Selector selector = Selector.open();

        // 创建服务端socket
        ServerSocketChannel ssc = ServerSocketChannel.open();
        // 服务端socket开启非阻塞模式
        ssc.configureBlocking(false);
        // 服务端socket注册到selector
        SelectionKey sscKey = ssc.register(selector, 0, null);
        // 关注accept成功接受连接建立事件
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        // 绑定端口号
        ssc.bind(new InetSocketAddress(8080));
        log.debug("sscKey：{}",sscKey);

        while(true) {
            // selector获取事件  无事件发生会阻塞
            selector.select();

            // selector监听到的事件
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while(iter.hasNext()){
                SelectionKey key = iter.next();
                log.debug("key：{}",key);
                // 必须删除监听到的事件，否则会驻留selectedKeys中，否则可能出现问题
                iter.remove();

                if(key.isAcceptable()){
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    ByteBuffer buffer = ByteBuffer.allocate(16);
                    SelectionKey scKey = sc.register(selector, 0, buffer);
                    scKey.interestOps(SelectionKey.OP_READ);
                    log.debug("after accepted...{}",sc);
                }else if(key.isReadable()){
                    try {
                        SocketChannel sc = (SocketChannel) key.channel();
                        log.debug("sc: {}",sc);
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        int read = sc.read(buffer);
                        if(read == -1){
                            // 客户端正常关闭，会触发读取事件，返回读取字节数-1，并且会尝试重复读取，直到读取到数据，所以也要取消key
                            key.cancel();
                        }else{
                            // 客户端正常读取数据
                            split(buffer);
                            if(buffer.position() == buffer.limit()){
                                ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                                buffer.flip();
                                newBuffer.put(buffer);
                                key.attach(newBuffer);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        // 客户端异常关闭，会抛出异常满，并且会触发读取事件
                        key.cancel();
                    }
                }
            }
        }
    }
}
