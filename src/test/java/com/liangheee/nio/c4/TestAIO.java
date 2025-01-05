package com.liangheee.nio.c4;

import com.liangheee.nio.c1.ByteBufferUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Slf4j
public class TestAIO {
    // 测试异步非阻塞IO
    public static void main(String[] args) throws IOException {
        AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get("data.txt"), StandardOpenOption.READ);
        ByteBuffer buffer = ByteBuffer.allocate(16);
        log.debug("before read...");
        // 调用回调方法的线程是一个守护线程
        // 如果主线程挂掉了，那么守护线程就会即使处于运行状态，也会立即停止运行
        channel.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                // result表示实际读取到的字节数
                log.debug("reading...{}",result);
                attachment.flip();
                ByteBufferUtil.debugAll(attachment);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                exc.printStackTrace();
            }
        });
        log.debug("after read...");
        System.in.read();
    }
}
