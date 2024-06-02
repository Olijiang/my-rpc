package rpc.loadbalance;

import rpc.entity.RpcRequest;
import rpc.entity.ServiceProfileList;
import rpc.entity.ServiceProfile;

import java.util.List;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: TODO
 * @Date: 2024/05/29/18:59
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    @Override
    public ServiceProfile select(RpcRequest rpcRequest, ServiceProfileList serviceProfileList) {
        List<ServiceProfile> serviceProfiles = serviceProfileList.getServiceProfiles();
        if (serviceProfiles.isEmpty()) {
            return null;
        }
        if (serviceProfiles.size() == 1) {
            return serviceProfiles.get(0);
        }
        return doSelect(rpcRequest, serviceProfileList);
    }


    protected abstract ServiceProfile doSelect(RpcRequest rpcRequest, ServiceProfileList serviceProfileList);
}
