package com.liangheee.util;

import java.util.concurrent.atomic.AtomicInteger;

public class SequenceIdGenerator {
    private static AtomicInteger generator = new AtomicInteger(0);

    public static int nextId(){
        return generator.incrementAndGet();
    }
}
