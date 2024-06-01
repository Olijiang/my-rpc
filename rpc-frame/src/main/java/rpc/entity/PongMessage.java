package rpc.entity;


import rpc.communication.netty.channel.InetSocket;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PongMessage extends Message{

    Map<String, Set<InetSocket>> services;
    @Override
    public int getMessageType() {
        return PongMessage;
    }
}
