package rpc.registry;

import rpc.entity.RpcRequest;
import rpc.entity.ServiceProfile;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: 提供服务发现
 * @Date: 2024/05/29/10:10
 */
public interface ServiceDiscovery {

    /**
     *
     * @param rpcRequest 请求对象
     * @return 服务封装对象
     */
    ServiceProfile lookupService(RpcRequest rpcRequest);


}
