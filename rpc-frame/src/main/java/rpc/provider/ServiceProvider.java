package rpc.provider;

import rpc.annotation.RpcService;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: 提供 和 收集 本应用中的服务信息
 * @Date: 2024/05/30/16:20
 */
public interface ServiceProvider {

    /**
     * 收集服务信息
     * @param service 服务实现类对象
     * @param rpcService 服务注解
     */
    void addService(Object service, RpcService rpcService);

    /**
     * 把本地服务注册到注册中心
     */
    void flushToRegistry();

    Object getService(String serviceName, String version);

}
