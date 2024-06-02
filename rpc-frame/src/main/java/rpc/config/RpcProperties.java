package rpc.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import rpc.annotation.EnableRpcServer;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: 配置类
 * @Date: 2024/05/29/21:28
 */

@Data
@Component
@ConfigurationProperties(prefix = "rpc")
public class RpcProperties {

    @Autowired
    ApplicationContext context;

    /**
     * 注册中心地址, 默认127.0.0.1：2181
     */
    private String registerAddress;

    /**
     * 序列化算法 Java Gson Kryo， 默认 Kryo
     */
    private String serializerAlgorithm;

    /**
     * 提供rpc服务的端口 默认 21812
     */
    private Integer serverPort;

    public static String REGISTER_ADDRESS = "127.0.0.1:2181";

    public static String SERIALIZER_ALGORITHM = "Kryo";

    public static Integer SERVER_PORT = 21818;


    @PostConstruct
    public void init() {
        if (this.serverPort != null) SERVER_PORT = this.serverPort;
        if (this.registerAddress != null && !this.registerAddress.isEmpty())
            REGISTER_ADDRESS = this.registerAddress;
        if (this.serializerAlgorithm != null && !this.serializerAlgorithm.isEmpty())
            SERIALIZER_ALGORITHM = this.serializerAlgorithm;

        String portKey = "--rpc.server.port=";
        String bootArgs = context.getEnvironment().getProperty("sun.java.command");
        String[] sourceArgs = bootArgs.split(" ");
        for (String arg : sourceArgs) {
            if (arg.startsWith(portKey)) {
                String port = arg.substring(portKey.length());
                SERVER_PORT = Integer.parseInt(port);
            }
        }

    }
}
