package rpc.spring;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;
import rpc.annotation.EnableRpcServer;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: 用于判断启动类上面是否有 EnableRpcServer 注解, 如果有的话就启动 rpc 服务器, 并触发服务注册
 * @Date: 2024/05/31/9:23
 */
public class EnableRpcServeCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        try {
            Map<String, Object> annotatedBeans = context.getBeanFactory().getBeansWithAnnotation(SpringBootApplication.class);
            String mainClassName = annotatedBeans.values().toArray()[0].getClass().getName();
            Class<?> mainClass = Class.forName(mainClassName);
            return mainClass.isAnnotationPresent(EnableRpcServer.class);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
}
