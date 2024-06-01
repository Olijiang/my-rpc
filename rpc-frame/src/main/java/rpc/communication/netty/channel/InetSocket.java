package rpc.communication.netty.channel;

import java.util.Objects;


public record InetSocket(String ip, Integer port) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InetSocket inetSocket)) return false;

        if (!Objects.equals(ip, inetSocket.ip)) return false;
        return Objects.equals(port, inetSocket.port);
    }

}
