package server.service;

import rpc.annotation.RpcService;
import service.UserService;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: TODO
 * @Date: 2024/05/31/16:15
 */
@RpcService(version = "1.1")
public class UserServiceImpl2 implements UserService {

    @Override
    public String getUser(String username) {
        return "User = {name:matin2, age:22}";
    }
}
