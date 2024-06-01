package client;

import client.service.TestHello;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import rpc.loadbalance.CreditBalancer;
import rpc.loadbalance.WeightedLoadBalance;

import java.util.List;


@SpringBootApplication
public class clientApp {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(clientApp.class);
        TestHello bean = context.getBean(TestHello.class);
        for (int i = 0; i < 20; i++) {
            bean.test();
        }
    }

}
