package rpc.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import rpc.communication.RpcClient;
import rpc.communication.RpcServer;
import rpc.communication.netty.channel.NettyClient;
import rpc.communication.netty.channel.NettyServer;
import rpc.loadbalance.LoadBalance;
import rpc.loadbalance.RandomLoadBalance;
import rpc.provider.ServiceProvider;
import rpc.provider.ZkServiceProvider;
import rpc.registry.ServiceDiscovery;
import rpc.registry.ServiceRegistry;
import rpc.registry.zk.ZkServiceDiscoveryImpl;
import rpc.registry.zk.ZkServiceRegistryImpl;
import rpc.spring.EnableRpcServeCondition;
import rpc.spring.RpcPostProcessor;
import rpc.spring.RpcServerRunner;

import javax.annotation.Resource;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: 自动配置类
 * @Date: 2024/05/29/21:36
 */
@Configuration
@EnableConfigurationProperties(RpcProperties.class)
public class RpcAutoConfiguration {

    @Resource
    private RpcProperties rpcProperties;

    @Bean
    @ConditionalOnMissingBean(ServiceProvider.class)
    @DependsOn("serviceRegistry")
    public ServiceProvider serviceProvider(ServiceRegistry serviceRegistry) {
        return new ZkServiceProvider(serviceRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(ServiceRegistry.class)
    public ServiceRegistry serviceRegistry() {
        return new ZkServiceRegistryImpl();
    }

    @Bean
    @ConditionalOnMissingBean(ServiceDiscovery.class)
    @DependsOn("loadBalance")
    public ServiceDiscovery serviceDiscovery(LoadBalance loadBalance) {
        return new ZkServiceDiscoveryImpl(loadBalance);
    }

    @Bean
    @ConditionalOnMissingBean(LoadBalance.class)
    public LoadBalance loadBalance() {
        return new RandomLoadBalance();
    }

    @Bean
    @ConditionalOnMissingBean(RpcClient.class)
    @DependsOn("serviceDiscovery")
    public RpcClient rpcClient(ServiceDiscovery serviceDiscovery) {
        return new NettyClient(serviceDiscovery);
    }

    @Bean
    @DependsOn({"rpcClient", "serviceProvider"})
    public RpcPostProcessor rpcPostProcessor(RpcClient rpcClient, ServiceProvider serviceProvider) {
        return new RpcPostProcessor(serviceProvider, rpcClient);
    }

    @Bean
    @DependsOn({"serviceProvider"})
    @Conditional(EnableRpcServeCondition.class)
    @ConditionalOnMissingBean(RpcServer.class)
    public RpcServer rpcServer(ServiceProvider serviceProvider) {
        return new NettyServer(serviceProvider);
    }

    @Bean
    public RpcServerRunner rpcServerRunner(ApplicationContext applicationContext) {
        return new RpcServerRunner(applicationContext);
    }
}
