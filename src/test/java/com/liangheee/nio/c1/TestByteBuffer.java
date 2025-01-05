package com.liangheee.nio.c1;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@Slf4j
public class TestByteBuffer {
    public static void main(String[] args) {
        try (FileChannel channel = new FileInputStream("data.txt").getChannel()) {
            // 创建ByteBuffer
            ByteBuffer byteBuffer = ByteBuffer.allocate(10);
            // 通过channel读取数据，将数据写入buffer
            int len;
            while ((len = channel.read(byteBuffer)) != -1) {
                log.debug("读取的字节数：{}",len);
                // ByteBuffer切换读模式
                byteBuffer.flip();
                while(byteBuffer.hasRemaining()){ // 如果ByteBuffer中写入的数据有剩余
                    log.debug("实际字节：{}",(char)byteBuffer.get());
                }
                // ByteBuffer切换写模式
                byteBuffer.clear();
            }
        } catch (IOException e) {
        }
    }
}
