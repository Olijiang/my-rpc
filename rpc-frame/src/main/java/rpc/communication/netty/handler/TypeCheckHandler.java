package rpc.communication.netty.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;
import rpc.entity.Message;


@Slf4j
@ChannelHandler.Sharable
public class TypeCheckHandler extends ChannelDuplexHandler {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof Message)) {
            // 收到的消息类型不正确，抛出异常或者记录错误日志
            // 例如：
            log.error("Received message is not of type Message, received type: {}", msg.getClass().getName());
            throw new IllegalArgumentException("Received message is not of type Message");
        }
        // 继续处理正确类型的消息
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof Message)) {
            // 发送的消息类型不正确，抛出异常或者记录错误日志
            // 例如：
            log.error("Sent message is not of type Message, sent type: {}", msg.getClass().getName());
            throw new IllegalArgumentException("Sent message is not of type Message");
        }
        // 继续发送正确类型的消息
        super.write(ctx, msg, promise);
    }
}
