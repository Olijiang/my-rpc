package client;

import client.service.TestHello;
import io.netty.channel.Channel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import rpc.communication.netty.channel.InetSocket;
import rpc.communication.netty.channel.NettyClient;
import rpc.loadbalance.LoadBalance;


import java.util.List;


@SpringBootApplication
public class clientApp {
// D:\Develop\apache-zookeeper-3.9.2-bin\bin\ZkCli.cmd -server 43.139.31.192:2181
// create /my-rpc/service.HelloService/providers/192.168.123.75:8888;-1;default;1
    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(clientApp.class);
        TestHello bean = context.getBean(TestHello.class);
//        for (int i = 0; i < 10; i++) {
//            bean.test();
//       }

        bean.test();

//        NettyClient nettyClient = context.getBean(NettyClient.class);
//        InetSocket socket = new InetSocket("127.0.0.1", 6666);
//        Channel channel = nettyClient.getChannel(socket);
////        bean1.close();
        context.close();

        Thread.sleep(5000);

        System.out.println(123);
    }

}
