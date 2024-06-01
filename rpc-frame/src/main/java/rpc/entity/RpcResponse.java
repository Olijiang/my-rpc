package rpc.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: TODO
 * @Date: 2024/04/30/9:43
 */

@EqualsAndHashCode(callSuper = true)
@Data

@ToString(callSuper = true)
public class RpcResponse extends Message {

    Integer code;

    String message;

    Object response;

    @Override
    public int getMessageType() {
        return RpcResponse;
    }
}
