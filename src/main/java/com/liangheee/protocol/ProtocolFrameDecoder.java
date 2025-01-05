package com.liangheee.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ProtocolFrameDecoder extends LengthFieldBasedFrameDecoder {
    public ProtocolFrameDecoder(){
        super(1024,12,4,0,0);
    }
}
