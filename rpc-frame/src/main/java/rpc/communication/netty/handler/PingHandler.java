package rpc.communication.netty.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import rpc.entity.PingMessage;
import rpc.entity.RpcResponse;

@Slf4j
@ChannelHandler.Sharable
public class PingHandler extends SimpleChannelInboundHandler<PingMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PingMessage msg) throws Exception {
        log.info("{}", msg);
        RpcResponse rpcResponse = new RpcResponse();
        ctx.writeAndFlush(rpcResponse);
    }

}
