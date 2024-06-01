package rpc.registry;

import rpc.entity.ServiceProfile;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: 提供服务注册
 * @Date: 2024/05/29/10:09
 */
public interface ServiceRegistry {

    /**
     * 注册服务
     * @param serviceProfile 服务封装对象
     */
    void registerService(ServiceProfile serviceProfile);


}
