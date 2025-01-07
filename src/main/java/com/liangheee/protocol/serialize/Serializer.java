package com.liangheee.protocol.serialize;

import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Type;
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
                Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec()).create();
                String json = gson.toJson(msg);
                return json.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public <T> T deSerialize(Class<T> clazz, byte[] bytes) {
                Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec()).create();
                String json = new String(bytes, StandardCharsets.UTF_8);
                return gson.fromJson(json,clazz);
            }
        }
    }

    class ClassCodec implements JsonSerializer<Class>, JsonDeserializer<Class> {

        @Override
        public Class deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                return Class.forName(json.getAsString());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public JsonElement serialize(Class src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.getName());
        }
    }

}
