package rpc.loadbalance;

import lombok.extern.slf4j.Slf4j;
import rpc.entity.RpcRequest;
import rpc.entity.ServiceProfile;

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


    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    // 根据信用建立一个索引权重数组
    private final Map<String, List<Integer>> creditMap = new ConcurrentHashMap<>();

    private final ExecutorService worker = Executors.newFixedThreadPool(1);


    @Override
    protected ServiceProfile doSelect(RpcRequest rpcRequest, ServiceListProfile serviceListProfile) {
        String key = rpcRequest.getInterfaceName() + ":" + rpcRequest.getVersion() + ":" + rpcRequest.getGroup();
        AtomicInteger atomicInteger = counters.computeIfAbsent(key, (k) -> new AtomicInteger(0));
        // 服务发送过变化, 重新生成映射数组
        List<ServiceProfile> serviceProfiles = serviceListProfile.getServiceProfiles();
        updateCreditMapIfNeeded(key, serviceListProfile);
        List<Integer> creditList = creditMap.get(key);
        if (creditList == null) updateCreditMapIfNeeded(key, serviceListProfile);
        int size = creditList.size();
        int index = atomicInteger.getAndIncrement();
        if (index % size == 0) {
            serviceListProfile.setCreditChange(true);
            updateCreditMapIfNeeded(key, serviceListProfile);
        }
        log.debug("负载均衡:{}, {}, {}, {}", index, size, creditList, serviceProfiles);
        return serviceProfiles.get(creditList.get(index % size));
    }


    private void updateCreditMapIfNeeded(String key, ServiceListProfile serviceListProfile) {
        AtomicInteger atomicInteger = counters.computeIfAbsent(key, (k) -> new AtomicInteger(0));
        List<ServiceProfile> serviceProfiles = serviceListProfile.getServiceProfiles();
        int index = atomicInteger.get();
        if (serviceListProfile.isWeightChange()) {
            if (atomicInteger.compareAndSet(index, index + 1)) {
                List<Integer> list = builderWeightMap(key, serviceProfiles);
                creditMap.put(key, list);
                serviceListProfile.setWeightChange(false);
                atomicInteger.decrementAndGet();
            }
            serviceListProfile.setCreditChange(false);
        }
    }

    /**
     * 根据权重生成一个元素的下标数组, 返回打乱之后的数组
     *
     * @param key
     * @param profiles
     */
    private List<Integer> builderWeightMap(String key, List<ServiceProfile> profiles) {
        List<Integer> index = new ArrayList<>();
        for (int i = 0; i < profiles.size(); i++) {
            ServiceProfile profile = profiles.get(i);
            for (int integer = 0; integer < profile.getCredit(); integer++) {
                index.add(i);
            }
        }
        Collections.shuffle(index);
        return index;
    }

    /**
     * 调用失败时信用减一
     *
     * @param rpcRequest
     * @param serviceProfile
     */
    @Override
    public void callFailed(RpcRequest rpcRequest, ServiceProfile serviceProfile) {
        worker.execute(() -> {
            String key = rpcRequest.getInterfaceName() + ":" + rpcRequest.getVersion() + ":" + rpcRequest.getGroup();
            Integer credit = serviceProfile.getCredit();
            if (credit == 0) return;
            if (creditMap.containsKey(key)) {
                List<Integer> list = creditMap.get(key);
                int size = list.size();
                if (Objects.equals(list.get(size - 1), serviceProfile.getIndex())) {
                    list.remove(size - 1);
                } else {
                    int pos = size - 1;
                    while (!Objects.equals(list.get(pos), serviceProfile.getIndex())) pos--;
                    list.set(pos, list.get(size - 1));
                    list.remove(size - 1);
                    serviceProfile.setCredit(credit - 1);
                    counters.get(key).decrementAndGet();
                }
            }
        });
    }

    @Override
    public void callSuccess(RpcRequest rpcRequest, ServiceProfile serviceProfile) {
        worker.execute(() -> {
            String key = rpcRequest.getInterfaceName() + ":" + rpcRequest.getVersion() + ":" + rpcRequest.getGroup();
            Integer credit = serviceProfile.getCredit();
            if (credit > 32) return;
            if (creditMap.containsKey(key)) {
                List<Integer> list = creditMap.get(key);
                list.add(serviceProfile.getIndex());
                serviceProfile.setCredit(credit + 1);
            }
        });
    }


    public static void main(String[] args) {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setInterfaceName("123");
        rpcRequest.setGroup("de");
        rpcRequest.setVersion("-1");
        ServiceListProfile serviceListProfile = new ServiceListProfile();
        List<ServiceProfile> list = new ArrayList<>();
        list.add(new ServiceProfile("127.0.0.1", 111, "-1", "de", 5, 255));
        list.add(new ServiceProfile("127.0.0.2", 222, "-1", "dede", 5, 255));
        serviceListProfile.setServiceProfiles(list);

        CreditBalancer balancer = new CreditBalancer();
        for (int i = 0; i < 10; i++) {
            System.out.println(balancer.doSelect(rpcRequest, serviceListProfile));
        }
    }


}
