package server.service;

import rpc.annotation.RpcService;
import service.HelloService;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: TODO
 * @Date: 2024/05/29/20:21
 */

@RpcService
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        return "hello " + name;
    }

}
