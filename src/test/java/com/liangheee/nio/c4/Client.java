package com.liangheee.nio.c4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class Client {
    public static void main(String[] args) throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("127.0.0.1", 8080));
        sc.write(Charset.defaultCharset().encode("0123456789abcdef\n0123456789\nhello\n"));
        System.in.read();
    }
}
