package rpc.provider;

import lombok.extern.slf4j.Slf4j;
import rpc.annotation.RpcService;
import rpc.entity.ServiceProfile;
import rpc.registry.ServiceRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: zk 实现的服务提供, 缓存本地提供的服务信息， 同时把信息上传到注册中心
 * @Date: 2024/05/30/15:49
 */
@Slf4j
public class ZkServiceProvider implements ServiceProvider {
    private static final Map<String, ServiceProfile> serviceMap = new ConcurrentHashMap<>();

    private final ServiceRegistry serviceRegistry;

    public ZkServiceProvider(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void addService(Object service, RpcService rpcService) {
        Class<?>[] interfaces = service.getClass().getInterfaces();
        String group = rpcService.group();
        String version = rpcService.version();
        int weight = rpcService.weight();
        for (Class<?> anInterface : interfaces) {
            String serviceName = anInterface.getName();
            ServiceProfile profile = new ServiceProfile(service, serviceName, version, group, weight);
            String key = serviceName + ":" + version;
            serviceMap.put(key, profile);
        }
    }

    @Override
    public void flushToRegistry() {
        for (ServiceProfile def : serviceMap.values()) {
            serviceRegistry.registerService(def);
        }

    }

    @Override
    public Object getService(String serviceName, String version) {
        ServiceProfile res = serviceMap.get(serviceName + ":" + version);
        if (res == null) throw new RuntimeException("not found " + serviceName + " with " + version);
        return res.getService();
    }
}


