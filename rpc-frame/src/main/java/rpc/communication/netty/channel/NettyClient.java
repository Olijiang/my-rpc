package rpc.communication.netty.channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import rpc.communication.RpcClient;
import rpc.communication.netty.handler.RpcResponseHandler;
import rpc.communication.netty.handler.TypeCheckHandler;
import rpc.communication.netty.protocol.MessageCodec;
import rpc.entity.RpcRequest;
import rpc.entity.ServiceProfile;
import rpc.exception.ServiceNotFoundException;
import rpc.registry.ServiceDiscovery;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: Netty 实现的请求客户端
 * @Date: 2024/05/15/9:37
 */

@Slf4j
public class NettyClient implements RpcClient {

    private static final Map<InetSocket, Channel> channels = new ConcurrentHashMap<>();

    private final ServiceDiscovery serviceDiscovery;

    private final EventExecutor eventExecutor;

    private Integer connectionTimeOut;
    private Integer maxRetry = 3;
    private Integer timeOut = 3;

    public NettyClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
        this.eventExecutor = new DefaultEventExecutor();
    }

    public Channel getChannel(InetSocket socket) {
        return channels.computeIfAbsent(socket, socket1 -> initChannel(socket));
    }

    @Override
    public Object SendRequest(RpcRequest rpcRequest) throws Throwable {
        int cur = 0;
        Promise<Object> promise = eventExecutor.newPromise();
        Throwable throwable = null;
        ServiceProfile profile = null;
        while (cur < maxRetry) {
            try {
                // ip:port
                profile = serviceDiscovery.lookupService(rpcRequest);
                InetSocket socket = new InetSocket(profile.getIp(), profile.getPort());
                Channel channel = getChannel(socket);
                RpcResponseHandler.PROMISE_MAP.put(rpcRequest.getSequenceId(), promise);
                channel.writeAndFlush(rpcRequest);
                // 等待结果, 时间到了就继续向下运行
                promise.await(timeOut, TimeUnit.SECONDS);
                if (!promise.isDone()) {
                    throwable = new TimeoutException("Timed out when calling service: " + rpcRequest.getInterfaceName() + ", address: " + profile.getIp());
                }
            } catch (ServiceNotFoundException e) {
                // 服务没有找到就不用重试了
                throw e;
            } catch (Exception e) {
                String msg = "An exception occurred while calling the service: " + rpcRequest.getInterfaceName() + ", with the reason being: " + e.getMessage();
                throwable = new RuntimeException(msg);
            }
            if (promise.isSuccess()) return promise.getNow();
            else {
                if (profile != null) profile.getLoadBalance().callFailed(rpcRequest, profile);
                log.warn(throwable.getMessage());
            }
            cur++;
        }
        // 请求没有发到对面的情况
        if (throwable != null) throw throwable;

        // 请求发送成功了,但对面调用失败的情况
        throw new RuntimeException(promise.cause());
    }

    private Channel initChannel(InetSocket socket) {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        MessageCodec messageCodec = new MessageCodec();
        TypeCheckHandler typeCheckHandler = new TypeCheckHandler();
        RpcResponseHandler rpcResponseHandler = new RpcResponseHandler();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(worker);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new IdleStateHandler(0, 30, 0));
                ch.pipeline().addLast(new ChannelDuplexHandler() {
                    @Override
                    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                        IdleStateEvent event = (IdleStateEvent) evt;
                        if (event.state() == IdleState.WRITER_IDLE) {
                            log.info("关闭连接：{}", ctx.channel());
                            ctx.channel().close();
                            channels.remove(socket);
                        }
                    }
                });
                ch.pipeline().addLast(MessageCodec.getDecoder());
                ch.pipeline().addLast(messageCodec);
                ch.pipeline().addLast(typeCheckHandler);
                ch.pipeline().addLast(rpcResponseHandler);
            }
        });
        Channel channel;
        try {
            channel = bootstrap.connect(socket.ip(), socket.port()).sync().channel();
            channel.closeFuture().addListener(future -> {
                worker.shutdownGracefully();
                channels.remove(socket);
            });
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return channel;
    }


    @PreDestroy
    public void close() {
        for (Channel channel : channels.values()) {
            channel.close();
        }
    }

    public static void main(String[] args) {

    }


}
