package rpc.communication.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import rpc.entity.PongMessage;


public class PongHandler  extends SimpleChannelInboundHandler<PongMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PongMessage msg) throws Exception {

    }

}
