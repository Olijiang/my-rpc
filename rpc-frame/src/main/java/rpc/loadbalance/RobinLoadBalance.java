package rpc.loadbalance;

import rpc.entity.RpcRequest;
import rpc.entity.ServiceProfileList;
import rpc.entity.ServiceProfile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: 轮询负载均衡器
 * @Date: 2024/05/29/19:38
 */
public class RobinLoadBalance extends AbstractLoadBalance {

    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    @Override
    protected ServiceProfile doSelect(RpcRequest rpcRequest, ServiceProfileList serviceProfileList) {
        String key = rpcRequest.getInterfaceName() + ":" + rpcRequest.getVersion() + ":" + rpcRequest.getGroup();
        AtomicInteger atomicInteger = counters.computeIfAbsent(key, (k) -> new AtomicInteger(0));
        List<ServiceProfile> serviceProfiles = serviceProfileList.getServiceProfiles();
        int size = serviceProfiles.size();
        return serviceProfiles.get(atomicInteger.getAndIncrement() % size);

    }

    @Override
    public void callFailed(RpcRequest rpcRequest, ServiceProfile serviceProfile) {

    }

    @Override
    public void callSuccess(RpcRequest rpcRequest, ServiceProfile serviceProfile) {

    }
}
