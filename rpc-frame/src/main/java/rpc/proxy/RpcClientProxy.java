package rpc.proxy;

import lombok.Data;
import rpc.communication.RpcClient;
import rpc.entity.RpcRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: 生成rpc字段的动态代理类
 * @Date: 2024/05/30/18:09
 */
@Data
public final class RpcClientProxy implements InvocationHandler {

    private String serviceName;
    private Class<?> targetClass;
    private String group;
    private String version;

    private final RpcClient client;

    @SuppressWarnings("unchecked")
    public <T> T getProxy() {
        return (T) Proxy.newProxyInstance(targetClass.getClassLoader(), new Class[]{targetClass}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setGroup(group);
        rpcRequest.setVersion(version);

        rpcRequest.setInterfaceName(targetClass.getName());
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setParameterTypes(method.getParameterTypes());
        rpcRequest.setParameters(args);
        return client.SendRequest(rpcRequest);
    }

    public RpcClientProxy(String serviceName, Class<?> targetClass, String group, String version, RpcClient client) {
        this.serviceName = serviceName;
        this.targetClass = targetClass;
        this.group = group;
        this.version = version;
        this.client = client;
    }

}
