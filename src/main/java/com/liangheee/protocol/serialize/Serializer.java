package com.liangheee.protocol.serialize;

import com.google.gson.Gson;

import java.io.*;
import java.nio.charset.StandardCharsets;

public interface Serializer {
    <T> byte[] serialize(T msg);

    <T> T deSerialize(Class<T> clazz,byte[] bytes);

    enum Algorithm implements Serializer{
        JAVA {
            @Override
            public <T> byte[] serialize(T object) {
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    new ObjectOutputStream(bos).writeObject(object);
                    return bos.toByteArray();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public <T> T deSerialize(Class<T> clazz, byte[] bytes) {
                try {
                    return  (T) new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        },
        JSON {
            @Override
            public <T> byte[] serialize(T msg) {
                String json = new Gson().toJson(msg);
                return json.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public <T> T deSerialize(Class<T> clazz, byte[] bytes) {
                String json = new String(bytes, StandardCharsets.UTF_8);
                return new Gson().fromJson(json,clazz);
            }
        }
    }

}
