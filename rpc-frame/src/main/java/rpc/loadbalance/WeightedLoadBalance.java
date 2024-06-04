package rpc.loadbalance;

import lombok.extern.slf4j.Slf4j;
import rpc.entity.RpcRequest;
import rpc.entity.ServiceProfileList;
import rpc.entity.ServiceProfile;
import rpc.exception.ServiceNotFoundException;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Snion
 * @version 1.0
 * @description: 加权轮询负载均衡器
 * @date 2024/6/1 18:55
 */
@Slf4j
public class WeightedLoadBalance extends AbstractLoadBalance {

    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    /**
     * 调用失败时加入这个 的黑名单
     */
    private final ExpiringSet<ServiceProfile> blackList = new ExpiringSet<>(3000);
    // 根据服务权重建立一个map表
    private final Map<ServiceProfileList, List<Integer>> weightMap = new ConcurrentHashMap<>();

    /**
     * 清理的循环周期
     */
    private final int clearCycle = 65536;

    /**
     * 清理的过去时间
     */
    private final int expire = 1000 * 60 * 60;

    @Override
    protected ServiceProfile doSelect(RpcRequest rpcRequest, ServiceProfileList serviceProfileList) {
        String key = rpcRequest.getServiceKey();
        serviceProfileList.updateTime();
        AtomicInteger atomicInteger = counters.computeIfAbsent(key, (k) -> new AtomicInteger(0));
        List<Integer> weightList = weightMap.computeIfAbsent(serviceProfileList, this::builderWeightTable);
        int size = weightList.size();
        ServiceProfile serviceProfile = null;
        List<ServiceProfile> serviceProfiles = serviceProfileList.getServiceProfiles();
        int cycleNum = 0;
        while (cycleNum < size && (serviceProfile == null || blackList.containsKey(serviceProfile))) {
            cycleNum++;
            int curCnt = atomicInteger.getAndIncrement();
            if (curCnt == clearCycle) clear();
            serviceProfile = serviceProfiles.get(weightList.get(curCnt % size));
        }
        if (cycleNum > size || serviceProfile == null) throw new ServiceNotFoundException("没有可用的服务");
        serviceProfile.setLoadBalance(this);
        log.debug("负载均衡:{}", serviceProfile);

        return serviceProfile;
    }

    /**
     * 根据权重生成一个元素的下标数组, 返回打乱之后的数组
     */
    private List<Integer> builderWeightTable(ServiceProfileList serviceProfileList) {
        List<ServiceProfile> profiles = serviceProfileList.getServiceProfiles();
        List<Integer> weightTable = new ArrayList<>();
        for (int i = 0; i < profiles.size(); i++) {
            ServiceProfile profile = profiles.get(i);
            for (int integer = 0; integer < profile.getWeight(); integer++) {
                weightTable.add(i);
            }
        }
        Collections.shuffle(weightTable);
        return weightTable;
    }

    /**
     * 每隔 cycle 周期清理一次 weightMap, 可能有已经访问不到的 serviceProfileList
     */
    private void clear() {
        Iterator<ServiceProfileList> iterator = weightMap.keySet().iterator();
        long now = System.currentTimeMillis();
        while (iterator.hasNext()) {
            ServiceProfileList next = iterator.next();
            if (now - next.getTimeStamp() > expire) iterator.remove();
        }
    }

    @PreDestroy
    public void close() {
        blackList.shutdown();
    }

    @Override
    public void callFailed(RpcRequest rpcRequest, ServiceProfile serviceProfile) {
        blackList.add(serviceProfile);
    }

    @Override
    public void callSuccess(RpcRequest rpcRequest, ServiceProfile serviceProfile) {

    }

    public static void main(String[] args) {
        ConcurrentHashMap<Integer, String> map = new ConcurrentHashMap<>();

        // 添加一些元素
        map.put(1, "One");
        map.put(2, "Two");
        map.put(3, "Three");

        // 创建一个线程用于并发修改映射
        Thread modifyThread = new Thread(() -> {
            for (int i = 4; i <= 10; i++) {
                map.put(i, "Value " + i);
            }
        });
        modifyThread.start();

        // 使用迭代器遍历映射
        Iterator<Integer> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            Integer next = iterator.next();
            System.out.println("Key: " + next);
            if (next == 2) iterator.remove();
        }

        // 等待修改线程结束
        try {
            modifyThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(map);
    }
}
