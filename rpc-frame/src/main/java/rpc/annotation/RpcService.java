package rpc.annotation;


import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 对外提供服务注解, 通过后处理器扫描并收集这些bean, 对外提供服务的话会把这些服务注册到注册中心
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Component
public @interface RpcService {

    String version() default "-1";

    String group() default "default";

    int weight() default 1;

}
