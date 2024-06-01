package rpc.communication.netty.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import rpc.entity.RpcRequest;
import rpc.entity.RpcResponse;
import rpc.provider.ServiceProvider;

import java.lang.reflect.Method;
import java.util.Arrays;


@Slf4j
@ChannelHandler.Sharable
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private final ServiceProvider serviceProvider;

    public RpcRequestHandler(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        log.info("{}", request);
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setSequenceId(request.getSequenceId());
        String methodName = request.getMethodName();
        String interfaceName = request.getInterfaceName();
        Object[] parameters = request.getParameters();
        Class<?>[] parameterTypes = request.getParameterTypes();
        try {
            //
            Object obj = serviceProvider.getService(request.getInterfaceName(), request.getVersion());
            Class<?> aClass = Class.forName(interfaceName);
            Method method = aClass.getMethod(methodName, parameterTypes);
            Object invoke = method.invoke(obj, parameters);
            rpcResponse.setCode(200);
            rpcResponse.setResponse(invoke);
        } catch (Exception e) {
            rpcResponse.setCode(400);
            String exMsg = e.getMessage();
            if (e.getCause() != null) exMsg = e.getCause().getMessage();
            String message = "An exception occurred while invoke the method: " + methodName + " in " + interfaceName + " with " + Arrays.toString(parameters) + "caused by: " + exMsg;
            rpcResponse.setResponse(message);
            log.warn(message);
        }
        ctx.writeAndFlush(rpcResponse);

    }
}
