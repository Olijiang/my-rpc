package rpc.entity;

import lombok.Data;
import rpc.loadbalance.LoadBalance;

/**
 * @author Snion
 * @version 1.0
 * @description: TODO
 * @date 2024/6/1 16:43
 */
@Data
public class ServiceProfile{

    Object service;
    String serviceName;

    String ip;
    Integer port;
    String version;
    String group;
    Integer weight;
    Integer credit;

    // 记住当前服务在服务列表中的下标位置
    Integer index;

    LoadBalance loadBalance;

    public ServiceProfile(String ip, Integer port, String version, String group, Integer weight, Integer credit) {
        this.ip = ip;
        this.port = port;
        this.version = version;
        this.group = group;
        this.weight = weight;
        this.credit = credit;
    }

    public ServiceProfile(Object service,String serviceName, String version, String group, Integer weight) {
        this.service = service;
        this.serviceName = serviceName;
        this.version = version;
        this.group = group;
        this.weight = weight;
    }
}
