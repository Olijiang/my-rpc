package rpc.registry.zk;

import rpc.entity.RpcRequest;
import rpc.entity.ServiceProfile;
import rpc.exception.ServiceNotFoundException;
import rpc.loadbalance.LoadBalance;
import rpc.registry.ServiceDiscovery;

import java.util.List;
import java.util.Map;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: zookeeper 服务发现
 * @Date: 2024/05/29/11:32
 */
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {


    private final LoadBalance loadBalance;

    public ZkServiceDiscoveryImpl(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    @Override
    public ServiceProfile lookupService(RpcRequest rpcRequest) {
        return loadBalance.select(rpcRequest, CuratorUtils.getService(rpcRequest));
    }



}
