package rpc.loadbalance;

import lombok.extern.slf4j.Slf4j;
import rpc.entity.RpcRequest;
import rpc.entity.ServiceListProfile;
import rpc.entity.ServiceProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Snion
 * @version 1.0
 * @description: 加权轮询负载均衡器
 * @date 2024/6/1 18:55
 */
@Slf4j
public class WeightedLoadBalance extends AbstractLoadBalance  {


    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    // 根据服务权重建立一个map表
    private final Map<String, List<Integer>> weightMap = new ConcurrentHashMap<>();

    @Override
    protected ServiceProfile doSelect(RpcRequest rpcRequest, ServiceListProfile serviceListProfile) {
        String key = rpcRequest.getInterfaceName() + ":" + rpcRequest.getVersion() + ":" + rpcRequest.getGroup();
        AtomicInteger atomicInteger = counters.computeIfAbsent(key, (k) -> new AtomicInteger(0));
        // 服务发送过变化, 重新生成映射数组
        List<ServiceProfile> serviceProfiles = serviceListProfile.getServiceProfiles();
        if (serviceListProfile.isCreditChange()) {
            builderWeightMap(key, serviceProfiles);
            serviceListProfile.setWeightChange(false);
        }
        List<Integer> weightList = weightMap.get(key);
        int size = weightList.size();
        int index = atomicInteger.getAndIncrement();
        log.warn("负载均衡:{}, {}, {}, {}", index, size, weightList, serviceProfiles);
        return serviceProfiles.get(weightList.get(index % size));
    }

    /**
     * 根据权重生成一个元素的下标数组, 返回打乱之后的数组
     *
     * @param key
     * @param profiles
     */
    private void builderWeightMap(String key, List<ServiceProfile> profiles) {
        if (!weightMap.containsKey(key)) {
            List<Integer> index = new ArrayList<>();
            for (int i = 0; i < profiles.size(); i++) {
                ServiceProfile profile = profiles.get(i);
                profile.setIndex(i);
                for (int integer = 0; integer < profile.getWeight(); integer++) {
                    index.add(i);
                }
            }
            Collections.shuffle(index);
            weightMap.put(key, index);
        }
    }

    @Override
    public void callFailed(RpcRequest rpcRequest, ServiceProfile serviceProfile) {

    }

    @Override
    public void callSuccess(RpcRequest rpcRequest, ServiceProfile serviceProfile) {

    }
}
