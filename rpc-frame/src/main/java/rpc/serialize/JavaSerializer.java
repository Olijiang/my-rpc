package rpc.serialize;

import rpc.exception.SerializeException;

import java.io.*;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: TODO
 * @Date: 2024/05/30/14:38
 */
public class JavaSerializer implements Serializer {

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new SerializeException("Deserialization failed");
        }
    }

    @Override
    public <T> byte[] serialize(T obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
        } catch (IOException e) {
            throw new SerializeException("Serialization failed");
        }
        return bos.toByteArray();
    }


}
