package rpc.communication.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import rpc.entity.RpcResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcResponse> {

    public static final Map<Integer, Promise<Object>> PROMISE_MAP = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        log.info("{}", msg);
        Promise<Object> promise = PROMISE_MAP.remove(msg.getSequenceId());
        if (promise != null) {
            if (msg.getCode() == 200){
                promise.setSuccess(msg.getResponse());
            }else {
                promise.setFailure(new RuntimeException(msg.getResponse().toString()));
            }
        }
    }
}
