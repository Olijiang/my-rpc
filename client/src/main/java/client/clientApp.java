package client;

import client.service.TestHello;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;


@SpringBootApplication
public class clientApp {
    // D:\Develop\apache-zookeeper-3.9.2-bin\bin\ZkCli.cmd -server 43.139.31.192:2181
// create /my-rpc/service.HelloService/providers/192.168.123.75:8888;-1;default;1
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(clientApp.class);
        TestHello bean = context.getBean(TestHello.class);

            for (int i = 0; i < 100; i++) {
                bean.test();
            }




    }

}
