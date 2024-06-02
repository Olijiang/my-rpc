package rpc.loadbalance;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Snion
 * @version 1.0
 * @description: 一个懒惰过期的 map
 * @date 2024/6/2 20:01
 */
public class ExpiringMap<K, V> {
    private final ConcurrentHashMap<K, ExpiringValue<V>> map;
    private final ScheduledExecutorService scheduler;
    private final long expirationTimeMillis;

    private record ExpiringValue<V>(V value, long expirationTime) {
    }

    public ExpiringMap(long expirationTimeMillis) {
        this.map = new ConcurrentHashMap<>();
        this.expirationTimeMillis = expirationTimeMillis;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        startCleanupTask();
    }

    public void put(K key, V value) {
        long expirationTime = System.currentTimeMillis() + expirationTimeMillis;
        map.put(key, new ExpiringValue<>(value, expirationTime));
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    public V get(K key) {
        ExpiringValue<V> expiringValue = map.get(key);
        if (expiringValue == null) {
            return null;
        }

        if (System.currentTimeMillis() > expiringValue.expirationTime()) {
            map.remove(key);
            return null;
        }

        return expiringValue.value();
    }

    private void startCleanupTask() {
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            for (K key : map.keySet()) {
                ExpiringValue<V> expiringValue = map.get(key);
                if (expiringValue != null && now > expiringValue.expirationTime()) {
                    map.remove(key);
                }
            }
        }, expirationTimeMillis, expirationTimeMillis * 10, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}