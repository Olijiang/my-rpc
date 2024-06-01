package server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import rpc.annotation.EnableRpcServer;


@EnableRpcServer
@SpringBootApplication
public class ServerApp {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ServerApp.class);
    }


}
