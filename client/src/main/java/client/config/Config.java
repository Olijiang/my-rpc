package client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rpc.loadbalance.CreditBalancer;
import rpc.loadbalance.WeightedLoadBalance;
import rpc.loadbalance.LoadBalance;

/**
 * @author Snion
 * @version 1.0
 * @description: TODO
 * @date 2024/6/1 19:43
 */
@Configuration
public class Config {
    @Bean
    public LoadBalance loadBalance() {
        return new CreditBalancer();
    }

}
