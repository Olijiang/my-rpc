package rpc.loadbalance;

import lombok.extern.slf4j.Slf4j;
import rpc.entity.RpcRequest;
import rpc.entity.ServiceProfileList;
import rpc.entity.ServiceProfile;
import rpc.exception.ServiceNotFoundException;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Snion
 * @version 1.0
 * @description: 基于信用制的负载均衡器
 * @date 2024/6/1 18:08
 */
@Slf4j
public class CreditBalancer extends AbstractLoadBalance {

    /**
     * 调用失败时加入这个 的黑名单
     */
    private final ExpiringSet<ServiceProfile> blackList = new ExpiringSet<>(3000);

    /**
     * 记录当前 rpcRequest 已经使用完了服务
     */
    private final Map<String, Set<ServiceProfile>> usedServiceMap = new ConcurrentHashMap<>();
    /**
     * 记录当前 rpcRequest 的调用次数
     */
    private final Map<String, AtomicInteger> useMap = new ConcurrentHashMap<>();

    private final ExecutorService worker = Executors.newFixedThreadPool(1);


    @Override
    protected ServiceProfile doSelect(RpcRequest rpcRequest, ServiceProfileList serviceProfileList) {
        String key = rpcRequest.getServiceKey();
        useMap.computeIfAbsent(key, (k) -> new AtomicInteger(0));
        usedServiceMap.computeIfAbsent(key, (k) -> ConcurrentHashMap.newKeySet());
        List<ServiceProfile> serviceProfiles = serviceProfileList.getServiceProfiles();
        int size = serviceProfiles.size();
        ServiceProfile serviceProfile = null;
        int cycleNum = 0; // 避免死循环
        while (cycleNum < size && (serviceProfile == null || blackList.containsKey(serviceProfile))) {
            AtomicInteger useCounter = useMap.computeIfAbsent(key, (k) -> new AtomicInteger(0));
            Set<ServiceProfile> usedSet = usedServiceMap.computeIfAbsent(key, (k) -> ConcurrentHashMap.newKeySet());
            int use = useCounter.get();
            if (useCounter.compareAndSet(use, use + 1)) {
                cycleNum++;
                int pos = use % size;
                ServiceProfile curService = serviceProfiles.get(pos);
                int level = use / size;
                if (usedSet.contains(curService)) continue;
                if (level < curService.getCredit()) serviceProfile = curService;
                else {
                    usedSet.add(curService);
                    // 重置服务
                    if (usedSet.size() == size) cycleNum = 0;
                    if (useCounter.compareAndSet(use + 1, 0)) usedSet.clear();
                }
            }
        }
        if (cycleNum > size || serviceProfile == null) throw new ServiceNotFoundException("没有可用的服务");
        serviceProfile.setLoadBalance(this);
        log.debug("负载均衡:{}, {}", serviceProfile, serviceProfiles);
        return serviceProfile;
    }


    /**
     * 调用失败时信用减 1
     *
     * @param rpcRequest
     * @param serviceProfile
     */
    @Override
    public void callFailed(RpcRequest rpcRequest, ServiceProfile serviceProfile) {
        worker.execute(() -> {
            int credit = serviceProfile.getCredit();
            if (credit == 0) return;
            serviceProfile.setCredit(credit - 1);
            blackList.add(serviceProfile);
            log.warn("服务调用失败:{}", serviceProfile);
        });
    }

    /**
     * 调用成功时信仰加 1
     *
     * @param rpcRequest
     * @param serviceProfile
     */
    @Override
    public void callSuccess(RpcRequest rpcRequest, ServiceProfile serviceProfile) {
        worker.execute(() -> {
            int credit = serviceProfile.getCredit();
            if (credit > 32) return;
            serviceProfile.setCredit(credit + 1);
        });
    }

    @PreDestroy
    @Override
    public void close() {
        worker.shutdown();
        blackList.shutdown();
        System.out.println("loadbalance 关闭");
    }

    public static void main(String[] args) {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setInterfaceName("123");
        rpcRequest.setGroup("de");
        rpcRequest.setVersion("-1");
        ServiceProfileList serviceProfileList = new ServiceProfileList();
        List<ServiceProfile> list = new ArrayList<>();
        list.add(new ServiceProfile("127.0.0.1", 111, "-1", "de", 5, 255));
        list.add(new ServiceProfile("127.0.0.2", 222, "-1", "dede", 5, 3));
        serviceProfileList.setServiceProfiles(list);

        CreditBalancer balancer = new CreditBalancer();
        for (int i = 0; i < 10; i++) {
            System.out.println(balancer.doSelect(rpcRequest, serviceProfileList));
        }
        balancer.close();
    }


}
