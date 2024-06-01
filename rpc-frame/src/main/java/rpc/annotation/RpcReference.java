package rpc.annotation;

import java.lang.annotation.*;

/**
 * 服务代理注入注解, 通过spring的bean后置处理器, 如果有bean的Field上有这个注解的话会注入rpc动态代理类
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface RpcReference {

    String version() default "-1";

    String group() default "default";
}
