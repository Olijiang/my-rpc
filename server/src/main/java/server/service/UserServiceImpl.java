package server.service;

import rpc.annotation.RpcService;
import service.UserService;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: TODO
 * @Date: 2024/05/31/16:15
 */
@RpcService(version = "1.0")
public class UserServiceImpl implements UserService {

    @Override
    public String getUser(String username) {
        return "User = {name:matin, age:21}";
    }
}
