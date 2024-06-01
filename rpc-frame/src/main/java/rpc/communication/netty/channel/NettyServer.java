package rpc.communication.netty.channel;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import rpc.communication.RpcServer;
import rpc.communication.netty.handler.PingHandler;
import rpc.communication.netty.handler.RpcRequestHandler;
import rpc.communication.netty.handler.TypeCheckHandler;
import rpc.communication.netty.protocol.MessageCodec;
import rpc.provider.ServiceProvider;

import javax.annotation.PreDestroy;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: Netty 实现的接收 rpc 请求的服务器
 * @Date: 2024/05/15/9:37
 */
@Slf4j
public class NettyServer implements RpcServer {

    private final ServiceProvider serviceProvider;

    private Channel channel;

    public NettyServer(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public void start(Integer port) {
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();
        TypeCheckHandler typeCheckHandler = new TypeCheckHandler();
        MessageCodec messageCodec = new MessageCodec();
        ServerBootstrap bootstrap = new ServerBootstrap();
        RpcRequestHandler rpcRequestHandler = new RpcRequestHandler(serviceProvider);
        PingHandler pingHandler = new PingHandler();
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.group(boss, worker);
        //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
        bootstrap.option(ChannelOption.SO_BACKLOG, 128);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new IdleStateHandler(35, 0, 0));
                ch.pipeline().addLast(new ChannelDuplexHandler() {
                    @Override
                    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                        IdleStateEvent event = (IdleStateEvent) evt;
                        if (event.state() == IdleState.READER_IDLE) {
                            log.info("关闭连接：{}", ctx.channel());
                            ctx.channel().close();
                        }
                    }
                });
                ch.pipeline().addLast(MessageCodec.getDecoder());
                ch.pipeline().addLast(messageCodec);
                ch.pipeline().addLast(typeCheckHandler);
                ch.pipeline().addLast(rpcRequestHandler);
                ch.pipeline().addLast(pingHandler);
            }
        });
        try {
            channel = bootstrap.bind(port).sync().channel();
            channel.closeFuture().addListener(future -> {
                worker.shutdownGracefully();
                boss.shutdownGracefully();
            });
        } catch (Exception e) {
            log.error("occur exception when start server:", e);
        }
    }

    @PreDestroy
    public void close() {
        if (channel != null) channel.close();
    }

}
