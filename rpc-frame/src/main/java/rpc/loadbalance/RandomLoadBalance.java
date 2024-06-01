package rpc.loadbalance;

import rpc.entity.RpcRequest;
import rpc.entity.ServiceProfile;

import java.util.List;
import java.util.Random;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: 随机负载均衡器
 * @Date: 2024/05/29/19:24
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected ServiceProfile doSelect(RpcRequest rpcRequest, ServiceListProfile serviceListProfile) {
        Random random = new Random();
        List<ServiceProfile> serviceProfiles = serviceListProfile.getServiceProfiles();
        return serviceProfiles.get(random.nextInt(serviceProfiles.size()));
    }

    @Override
    public void callFailed(RpcRequest rpcRequest, ServiceProfile serviceProfile) {

    }

    @Override
    public void callSuccess(RpcRequest rpcRequest, ServiceProfile serviceProfile) {

    }
}
