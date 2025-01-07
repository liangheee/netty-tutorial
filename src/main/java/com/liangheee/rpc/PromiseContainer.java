package com.liangheee.rpc;

import io.netty.util.concurrent.Promise;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PromiseContainer {
    private static final Map<Integer, Promise<Object>> container = new ConcurrentHashMap<Integer, Promise<Object>>();

    public static Promise<Object> get(int sequenceId) {
        return container.get(sequenceId);
    }

    public static void put(Integer sequenceId, Promise<Object> promise) {
        System.out.println("执行put");
        container.put(sequenceId,promise);
    }

    public static Promise<Object> remove(Integer sequenceId){
        return container.remove(sequenceId);
    }
}
