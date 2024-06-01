package rpc.entity;


import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@ToString(callSuper = true)
public class PingMessage extends Message {

    String serviceName;
    String serviceIp;
    Integer servicePort;


    @Override
    public int getMessageType() {
        return PingMessage;
    }
}
