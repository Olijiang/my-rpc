package rpc.entity;

import lombok.Data;
import rpc.loadbalance.LoadBalance;

/**
 * @author Snion
 * @version 1.0
 * @description: 服务的封装对象
 * @date 2024/6/1 16:43
 */
@Data
public class ServiceProfile {

    // 服务端使用的属性
    Object service;
    String serviceName;

    // 客户端使用的属性
    String ip;
    int port;
    String version;
    String group;
    int weight;
    volatile int credit;

    // 负载均衡使用的属性
    LoadBalance loadBalance;


    public ServiceProfile(String ip, Integer port, String version, String group, Integer weight, Integer credit) {
        this.ip = ip;
        this.port = port;
        this.version = version;
        this.group = group;
        this.weight = weight;
        this.credit = credit;
    }

    public ServiceProfile(Object service, String serviceName, String version, String group, Integer weight) {
        this.service = service;
        this.serviceName = serviceName;
        this.version = version;
        this.group = group;
        this.weight = weight;
    }

    public void updateTimeStamp(){

    }
}
