package rpc.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import rpc.annotation.RpcReference;
import rpc.annotation.RpcService;
import rpc.communication.RpcClient;
import rpc.provider.ServiceProvider;
import rpc.proxy.RpcClientProxy;

import java.lang.reflect.Field;


/**
 *
 * @Author: zhangGuobin
 * @Date: 2024/5/31 21:56
 * @Description: bean 后置处理器, 两个作用
 * 1. 保存有 RpcService 注解的 bean,
 * 2. 给 bean 中有 RpcReference 修饰的 Field 注入代理实现
 */
@Slf4j
public class RpcPostProcessor implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;
    private final RpcClient client;

    public RpcPostProcessor(ServiceProvider serviceProvider, RpcClient client) {
        this.serviceProvider = serviceProvider;
        this.client = client;
    }
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 服务注册
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("found service: {}", bean.getClass().getName());
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            serviceProvider.addService(bean, rpcService);
            return bean;
        }

        // 代理注入
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                String group = rpcReference.group();
                String version = rpcReference.version();
                String serviceName = declaredField.getType().getName();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(serviceName, declaredField.getType(), group, version, client);
                Object proxy = rpcClientProxy.getProxy();
                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}
