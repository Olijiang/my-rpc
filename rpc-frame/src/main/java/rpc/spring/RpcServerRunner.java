package rpc.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import rpc.communication.RpcServer;
import rpc.config.RpcProperties;
import rpc.provider.ServiceProvider;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: spring 启动后的执行器, 检测是否需要启动 rpc 服务器 和 注册服务到注册中心
 * @Date: 2024/05/31/9:50
 */
@Slf4j
public class RpcServerRunner implements ApplicationRunner {

    ApplicationContext applicationContext;

    public RpcServerRunner(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) {
        try {
            RpcServer server = applicationContext.getBean(RpcServer.class);
            server.start(RpcProperties.SERVER_PORT);
            log.info("Rpc server start successfully on port " + RpcProperties.SERVER_PORT);
        } catch (Exception e) {
            log.info("No RPC server found");
            return;
        }
        try {
            ServiceProvider serviceProvider = applicationContext.getBean(ServiceProvider.class);
            serviceProvider.flushToRegistry();
            log.info("Rpc service register successfully");
        } catch (Exception e) {
            log.info("occurred exception when start server:{}", e.getMessage());
        }

    }
}
