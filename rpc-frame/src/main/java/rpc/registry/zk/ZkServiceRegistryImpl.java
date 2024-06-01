package rpc.registry.zk;

import lombok.extern.slf4j.Slf4j;
import rpc.config.RpcProperties;
import rpc.entity.ServiceProfile;
import rpc.registry.ServiceRegistry;

import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: TODO
 * @Date: 2024/05/29/11:30
 */
@Slf4j
public class ZkServiceRegistryImpl implements ServiceRegistry {

    private String ip;

    public ZkServiceRegistryImpl() {
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }

    @Override
    public void registerService(ServiceProfile profile) {
        String path = profile.getServiceName() + "/providers/" + ip + ":" + RpcProperties.SERVER_PORT + ";" + profile.getVersion() + ";" + profile.getGroup() + ";" + profile.getWeight();
        CuratorUtils.createEphemeralNode(path);
    }

    @PreDestroy
    public void registerClose() {
        CuratorUtils.close();
    }

    public static String getHostIp() {
        try {
            Process process = Runtime.getRuntime().exec("hostname -I");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            System.out.println(reader.readLine());
            return reader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println(getHostIp());
    }
}
