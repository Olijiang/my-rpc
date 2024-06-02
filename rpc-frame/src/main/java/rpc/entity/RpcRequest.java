package rpc.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: TODO
 * @Date: 2024/04/30/9:42
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class RpcRequest extends Message{

    private String interfaceName;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;
    private String version;
    private String group;

    @Override
    public int getMessageType() {
        return RpcRequest;
    }

    public RpcRequest() {
        this.sequenceId = counter.getAndIncrement();
    }

    public String getServiceKey(){
        return interfaceName + ":" + version + ":" + group;
    }
}
