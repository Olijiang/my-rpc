package rpc.serialize;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: 序列化器
 * @Date: 2024/04/29/9:42
 */
public interface Serializer {

    <T> T deserialize(Class<T> clazz, byte[] bytes);

    <T> byte[] serialize(T obj);


    enum Algorithm {
        Java, Gson, Kryo;
        private final Serializer serializer;

        Algorithm(){
            String name = this.name();
            serializer = switch (name){
                case "Java" ->  new JavaSerializer();
                case "Gson" ->  new GsonSerializer();
                case "Kryo" ->  new KryoSerializer();
                default -> throw new IllegalStateException("Unexpected value: " + name);
            };
        }

        public Serializer getSerializer(){
            return serializer;
        }

    }
}
