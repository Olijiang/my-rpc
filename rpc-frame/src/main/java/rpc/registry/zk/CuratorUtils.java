package rpc.registry.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import rpc.config.RpcProperties;
import rpc.entity.RpcRequest;
import rpc.entity.ServiceProfile;
import rpc.exception.ServiceNotFoundException;
import rpc.loadbalance.ServiceListProfile;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @Author: ZGB
 * @version: 1.0
 * @Description: 提供对 zookeeper 的常用操作, 会缓存已经拿到的路由信息, 同时监听已经获取路由变化做增量更新
 * @Date: 2024/05/29/11:54
 */
@Slf4j
public class CuratorUtils {

    public static final String REGISTER_ROOT_PATH = "/my-rpc/";
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();

    // 一级 serviceName, 二级 version, 三级 groupName
    private static final Map<String, Map<String, Map<String, List<ServiceProfile>>>> SERVICE_MAP = new ConcurrentHashMap<>();

    // 根据服务信息缓存满足要求的服务
    private static final Map<String, ServiceListProfile> SERVICE_LIST_PROFILE_MAP = new ConcurrentHashMap<>();

    public static CuratorFramework zkClient;
    public static Integer MAX_CREDIT = 32;


    private static CuratorFramework getZkClient() {
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000, 3);
        zkClient = CuratorFrameworkFactory.builder()
                // the server to connect to (can be a server list)
                .connectString(RpcProperties.REGISTER_ADDRESS)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        try {
            // wait 30s until connect to the zookeeper
            if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RuntimeException("Time out waiting to connect to ZK!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zkClient;
    }

    /**
     * 获取这个 serviceName 对于的服务节点
     * @param serviceName 服务名称
     * @return 服务信息的列表  ip:port;version;group;weight   credit
     */
    public static List<String> getChildrenNodes(String serviceName) {
        List<String> children = null;
        String servicePath = REGISTER_ROOT_PATH + serviceName + "/providers";
        try {
            // 节点类型 ip:version:group
            children = getZkClient().getChildren().forPath(servicePath);
            log.debug("getChildrenNodes： service: {}, child:{} ", serviceName, children);
            // 监听节点变化
            registerWatcher(servicePath);
            // 消费者登记
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            servicePath = serviceName + "/consumers/" + hostAddress;
            createEphemeralNode(servicePath);
        } catch (Exception e) {
            log.error("get children nodes for path [{}] fail, caused by {}", servicePath, e.getMessage());
        }
        return children;
    }

    /**
     * 获取这个
     * @param serviceName 服务名
     * @return 获取服务的 map , 一级 key为 version, 二级 key 为 group
     */
    public static Map<String, Map<String, List<ServiceProfile>>> getServerMap(String serviceName) {
        if (SERVICE_MAP.containsKey(serviceName)) {
            return SERVICE_MAP.get(serviceName);
        }
        List<String> children = getChildrenNodes(serviceName);
        Map<String, Map<String, List<ServiceProfile>>> versionMap = new ConcurrentHashMap<>();
        for (String child : children) {
            ServiceProfile profile = parserService(child);
            Map<String, List<ServiceProfile>> groupMap = versionMap.getOrDefault(profile.getVersion(), new ConcurrentHashMap<>());
            List<ServiceProfile> ipList = groupMap.getOrDefault(profile.getGroup(), new ArrayList<>());
            ipList.add(profile);
            groupMap.put(profile.getGroup(), ipList);
            versionMap.put(profile.getVersion(), groupMap);
        }
        SERVICE_MAP.put(serviceName, versionMap);
        return versionMap;
    }

    /**
     * 根据请求对象拿到满足要求的 服务列表封装对象
     * @param request 请求对象
     * @return ServiceListProfile
     */
    public static ServiceListProfile getService(RpcRequest request) {
        String serviceName = request.getInterfaceName();
        String version = request.getVersion();
        String group = request.getGroup();
        String key = request.getInterfaceName() + ":" + request.getVersion() + ":" + request.getGroup();
        if (SERVICE_LIST_PROFILE_MAP.containsKey(key)){
            ServiceListProfile serviceListProfile = SERVICE_LIST_PROFILE_MAP.get(key);
            serviceListProfile.setCreditChange(false);
            serviceListProfile.setWeightChange(false);
            return serviceListProfile;
        }else {
            // 根据服务名获取服务
            Map<String, Map<String, List<ServiceProfile>>> serviceMap = getServerMap(serviceName);
            if (serviceMap == null) throw new RuntimeException("No " + serviceName + " found");
            // 找对应版本的服务
            Map<String, List<ServiceProfile>> versionMap = serviceMap.get(version);
            if (versionMap == null) {
                String msg = "Service not found:" + serviceName + " found" + ((version.equals("-1")) ? "" : " with version " + version);
                throw new ServiceNotFoundException(msg);
            }
            ServiceListProfile serviceListProfile = new ServiceListProfile();
            // 先找同组服务
            List<ServiceProfile> profilesInGroup = versionMap.get(group);
            if (profilesInGroup == null || profilesInGroup.isEmpty()) {
                // 没有同组的服务, 找其他组的
                profilesInGroup = versionMap.values().stream().flatMap(List::stream).toList();
                if (profilesInGroup.isEmpty()) throw new ServiceNotFoundException("Service not found:" + serviceName);
            }
            serviceListProfile.setServiceProfiles(profilesInGroup);
            SERVICE_LIST_PROFILE_MAP.put(key, serviceListProfile);
            return serviceListProfile;
        }
    }

    public static ServiceProfile parserService(String info) {
        //  ip:port;version;group;weight   credit
        String[] split = info.split(";");
        String address = split[0];
        String[] split1 = address.split(":");
        String ip = split1[0];
        Integer port = Integer.parseInt(split1[1]);
        String version = split[1];
        String group = split[2];
        Integer weight = Integer.parseInt(split[3]);
        Integer credit = 32;
        return new ServiceProfile(ip, port, version, group, weight, credit);
    }

    public static void createEphemeralNode(String path) {
        path = REGISTER_ROOT_PATH + path;
        try {
            if (REGISTERED_PATH_SET.contains(path) || getZkClient().checkExists().forPath(path) != null) {
                log.debug("The node already exists. The node is:[{}]", path);
            } else {
                getZkClient().create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, MAX_CREDIT.toString().getBytes());
                log.debug("The node was created successfully. The node is:[{}]", path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            log.error("create persistent node for path [{}] fail,  caused by {}", path, e.getMessage());
        }
    }


    public static void registerWatcher(String servicePath) {
        CuratorCache cache = CuratorCache.build(getZkClient(), servicePath);
        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forChanges((oldNode, node) -> log.info("change : {}", node.toString()))
                .forCreates(childData -> log.info("create : {}", childData.toString()))
                .forDeletes(childData -> log.info("delete : {}", childData.toString()))
                .build();
        cache.listenable().addListener(listener);
        cache.start();
    }

    public static void close() {
        if (zkClient != null) zkClient.close();
    }
}
