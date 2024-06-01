package rpc.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


@Data
public abstract class Message implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static byte msgVersion = 1;

    static AtomicInteger counter = new AtomicInteger(0);

    int sequenceId;

    public static final int PingMessage = 1;
    public static final int PongMessage = 2;
    public static final int RpcRequest = 3;
    public static final int RpcResponse = 4;

    private static final Map<Integer, Class<?>> messageClasses = new HashMap<>();


    static {
        messageClasses.put(PingMessage, PingMessage.class);
        messageClasses.put(PongMessage, PongMessage.class);
        messageClasses.put(RpcRequest, RpcRequest.class);
        messageClasses.put(RpcResponse, RpcResponse.class);
    }

    public abstract int getMessageType();

    public static Class<?> getMessageClass(int type) {
        return messageClasses.get(type);
    }

}
