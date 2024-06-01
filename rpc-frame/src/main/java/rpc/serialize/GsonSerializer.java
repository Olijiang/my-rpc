package rpc.serialize;

import com.google.gson.*;
import rpc.exception.SerializeException;

import java.lang.reflect.Type;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: TODO
 * @Date: 2024/05/30/14:34
 */
public class GsonSerializer implements Serializer {
    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new ClassJsonCodec()).create();
        String json = new String(bytes);
        return gson.fromJson(json, clazz);
    }

    @Override
    public <T> byte[] serialize(T obj) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new ClassJsonCodec()).create();
        String json = gson.toJson(obj);
        return json.getBytes();
    }

    static class ClassJsonCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

        @Override
        public Class<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            try {
                return Class.forName(jsonElement.getAsString());
            } catch (ClassNotFoundException e) {
                throw new SerializeException("Deserialization failed");
            }
        }

        @Override
        public JsonElement serialize(Class<?> aClass, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(aClass.getName());
        }
    }
}
