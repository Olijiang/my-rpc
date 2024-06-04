package rpc.communication;

import rpc.entity.RpcRequest;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: TODO
 * @Date: 2024/05/30/19:52
 */
public interface RpcClient {

    Object SendRequest(RpcRequest rpcRequest) throws Throwable;

    void close();
}
