package rpc.communication.netty.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;
import rpc.config.RpcProperties;
import rpc.entity.Message;
import rpc.serialize.Serializer;

import java.util.List;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: 消息编解码器
 * @Date: 2024/05/09/19:33
 */
@Slf4j
@ChannelHandler.Sharable
public class MessageCodec extends MessageToMessageCodec<ByteBuf, Message> {

    private static final byte[] MAGIC_NUMBER = {0xc, 0xa, 0xf, 0xe};

    // message 的拆包器
    public static MyLengthFieldBasedFrameDecoder getDecoder() {
        return new MyLengthFieldBasedFrameDecoder(
                1024, 12, 4, 0, 0, MAGIC_NUMBER);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        ByteBuf byteBuf = ctx.alloc().buffer();
        // 魔术数 4
        byteBuf.writeBytes(MAGIC_NUMBER);
        // 版本号 1
        byteBuf.writeByte(Message.msgVersion);
        // 序列化id 4
        byteBuf.writeInt(msg.getSequenceId());
        // 序列号方式 1
        Serializer.Algorithm serializerAlgorithm = Serializer.Algorithm.valueOf(RpcProperties.SERIALIZER_ALGORITHM);
        byteBuf.writeByte(serializerAlgorithm.ordinal());
        // 指令类型 1
        byteBuf.writeByte(msg.getMessageType());
        // 长度填充 1
        byteBuf.writeByte(0xff);
        // 序列化
        byte[] bytes = serializerAlgorithm.getSerializer().serialize(msg);
        // 写入长度
        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
        out.add(byteBuf);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        if (msg.readableBytes() < MAGIC_NUMBER.length) {
            // 收到的消息长度不够，可以抛出异常或者记录错误日志
            // 例如：
            log.error("Received message is too short, expected at least {} bytes but got {}", MAGIC_NUMBER.length, msg.readableBytes());
            // 抛出异常或者直接返回，视情况而定
            return;
        }
        byte[] magicNum = new byte[4];
        msg.readBytes(magicNum, 0, 4);
        byte msgVersion = msg.readByte();
        int sequenceId = msg.readInt();
        byte serializerAlgorithm = msg.readByte();
        byte messageType = msg.readByte();
        msg.readByte();
        int length = msg.readInt();
        byte[] bytes = new byte[length];
        msg.readBytes(bytes, 0, length);

        Class<?> messageClass = Message.getMessageClass(messageType);
        Serializer.Algorithm algorithm = Serializer.Algorithm.values()[serializerAlgorithm];
        Message message = (Message) algorithm.getSerializer().deserialize(messageClass, bytes);
        out.add(message);
    }

    // 把异常暴露出去
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}

