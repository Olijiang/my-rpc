package rpc.loadbalance;

import lombok.extern.slf4j.Slf4j;
import rpc.entity.RpcRequest;
import rpc.entity.ServiceProfileList;
import rpc.entity.ServiceProfile;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
     * 记录当前位置的使用量
     */
    private final Map<String, AtomicInteger> useMap = new ConcurrentHashMap<>();

    /**
     * 记录服务当前访问位置
     */
    private final Map<String, AtomicInteger> posMap = new ConcurrentHashMap<>();
    private final ExecutorService worker = Executors.newFixedThreadPool(1);


    @Override
    protected ServiceProfile doSelect(RpcRequest rpcRequest, ServiceProfileList serviceProfileList) {
        String key = rpcRequest.getServiceKey();
        AtomicInteger useCounter = useMap.computeIfAbsent(key, (k) -> new AtomicInteger(0));
        AtomicInteger posCounter = posMap.computeIfAbsent(key, (k) -> new AtomicInteger(0));
        List<ServiceProfile> serviceProfiles = serviceProfileList.getServiceProfiles();
        int size = serviceProfiles.size();
        ServiceProfile serviceProfile = null;
        while (serviceProfile == null) {
            int use = useCounter.get();
            int pos = posCounter.get();
            ServiceProfile curService = serviceProfiles.get(pos);
            if (use < curService.getCredit()) {
                if (useCounter.compareAndSet(use, use + 1)) {
                    serviceProfile = curService;
                }
            } else {
                if (useCounter.compareAndSet(use, 0)) {
                    int newPos = (pos == size - 1) ? 0 : pos + 1;
                    posCounter.compareAndSet(pos, newPos);
                    serviceProfile = serviceProfiles.get(pos);
                }
            }
        }
        serviceProfile.setLoadBalance(this);
//        log.warn("负载均衡:{}, {}, {}", posCounter.get(), size, serviceProfiles);
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
    public void close(){
        worker.shutdown();
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
    }


}
