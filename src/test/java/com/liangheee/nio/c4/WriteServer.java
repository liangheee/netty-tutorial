package com.liangheee.nio.c4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class WriteServer {
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        SelectionKey sscKey = ssc.register(selector, 0, null);
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        ssc.bind(new InetSocketAddress(8080));

        while(true){
            selector.select();

            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while(iter.hasNext()){
                SelectionKey key = iter.next();
                iter.remove();

                if(key.isAcceptable()){
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    SelectionKey scKey = sc.register(selector, 0, null);

                    StringBuilder sb = new StringBuilder();
                    for(int i = 0;i < 30000000;i++){
                        sb.append('a');
                    }

                    ByteBuffer buffer = Charset.defaultCharset().encode(sb.toString());
                    // 数据首先写入网络输出缓冲区，如果一次性没有写完，会持续触发写事件
                    int write = sc.write(buffer);
                    System.out.println("实际写入的字节数：" + write);
                    if(buffer.hasRemaining()){
                        scKey.interestOps(scKey.interestOps() + SelectionKey.OP_WRITE);
                        scKey.attach(buffer);
                    }

                }else if(key.isWritable()){
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    SocketChannel sc = (SocketChannel) key.channel();
                    int write = sc.write(buffer);
                    System.out.println("实际写入的字节数：" + write);
                    if(!buffer.hasRemaining()){
                        key.interestOps(key.interestOps() - SelectionKey.OP_WRITE);
                        key.attach(null);
                    }
                }
            }
        }
    }
}
