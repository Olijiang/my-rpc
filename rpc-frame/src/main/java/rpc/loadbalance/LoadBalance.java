package rpc.loadbalance;

import rpc.entity.RpcRequest;
import rpc.entity.ServiceProfile;

import java.util.List;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: TODO
 * @Date: 2024/05/29/18:56
 */
public interface LoadBalance {

    ServiceProfile select(RpcRequest rpcRequest, ServiceListProfile serviceListProfile);

    void callFailed(RpcRequest rpcRequest, ServiceProfile serviceProfile);

    void callSuccess(RpcRequest rpcRequest, ServiceProfile serviceProfile);

}
