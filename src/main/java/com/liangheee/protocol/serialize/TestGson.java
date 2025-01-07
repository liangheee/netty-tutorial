package com.liangheee.protocol.serialize;

import com.google.gson.*;

import java.lang.reflect.Type;

public class TestGson {
    public static void main(String[] args) {
        ClassTypeAdapter adapter = new ClassTypeAdapter();
        Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, adapter).create();
        String json = gson.toJson(String.class);
        System.out.println(json);
        String res = gson.fromJson(json, String.class);
        System.out.println(res);
    }

    static class ClassTypeAdapter implements JsonSerializer<Class>, JsonDeserializer<Class> {

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
            String name = src.getName();
            return context.serialize(name);
        }
    }
}
