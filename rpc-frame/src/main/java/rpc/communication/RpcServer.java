package rpc.communication;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: TODO
 * @Date: 2024/05/31/9:44
 */
public interface RpcServer {

    void start(Integer port) throws InterruptedException;
}
