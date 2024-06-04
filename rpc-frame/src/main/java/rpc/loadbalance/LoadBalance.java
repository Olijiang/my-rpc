package rpc.loadbalance;

import rpc.entity.RpcRequest;
import rpc.entity.ServiceProfileList;
import rpc.entity.ServiceProfile;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: TODO
 * @Date: 2024/05/29/18:56
 */
public interface LoadBalance {

    ServiceProfile select(RpcRequest rpcRequest, ServiceProfileList serviceProfileList);

    void callFailed(RpcRequest rpcRequest, ServiceProfile serviceProfile);

    void callSuccess(RpcRequest rpcRequest, ServiceProfile serviceProfile);

    void close();
}
