package client.service;

import org.springframework.stereotype.Component;
import rpc.annotation.RpcReference;
import service.HelloService;
import service.UserService;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: TODO
 * @Date: 2024/05/29/20:24
 */
@Component
public class TestHello {

    @RpcReference
    HelloService helloService;

    @RpcReference(version = "1.0")
    UserService userService;

    @RpcReference(version = "1.1")
    UserService userService2;

    public String test(){
        return helloService.hello("小明");
    }

    public String getUser(){
        return userService.getUser("小美");
    }

    public String getUser2(){
        return userService2.getUser("小芳");
    }

}
