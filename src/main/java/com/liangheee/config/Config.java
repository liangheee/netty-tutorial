package com.liangheee.config;

import com.liangheee.protocol.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class Config {
    private static Properties properties;
    static {
        try (InputStream resourceAsStream = Config.class.getResourceAsStream("/application.properties")) {
            properties = new Properties();
            properties.load(resourceAsStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int serverPort(){
        String port = properties.getProperty("server.port");
        if(port == null || port.trim().isEmpty()){
            return 8080;
        }else{
            return Integer.parseInt(port);
        }
    }

    public static Serializer.Algorithm serDeAlgorithm(){
        String serDe = properties.getProperty("protocol.serializer");
        try {
            return Serializer.Algorithm.valueOf(serDe);
        } catch (IllegalArgumentException e) {
            log.debug("不存在该序列化方式，默认采用JDK序列化方式");
            return Serializer.Algorithm.JAVA;
        }
    }
}
