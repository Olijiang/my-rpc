package rpc.loadbalance;

import rpc.util.DaemonThreadFactory;

import javax.annotation.PreDestroy;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author Snion
 * @version 1.0
 * @description: 一个懒惰过期的 map
 * @date 2024/6/2 20:01
 */
public class ExpiringSet<V> {
    private final Set<V> set;
    private final Queue<ExpiringValue<V>> queue;
    private final ScheduledExecutorService scheduler;
    private final long expirationTimeMillis;

    private record ExpiringValue<V>(V value, long time) {
    }

    public ExpiringSet(long expirationTimeMillis) {
        this.set = ConcurrentHashMap.newKeySet();
        this.queue = new ConcurrentLinkedDeque<>();
        this.expirationTimeMillis = expirationTimeMillis;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("expiringSet-scheduler"));
        startCleanupTask();
    }

    public void add(V value) {
        long expirationTime = System.currentTimeMillis() + expirationTimeMillis;
        queue.offer(new ExpiringValue<>(value, expirationTime));
        set.add(value);
    }

    public boolean containsKey(V value) {
        return set.contains(value);
    }


    private void startCleanupTask() {
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            while (!queue.isEmpty() && queue.peek().time < now) {
                ExpiringValue<V> poll = queue.poll();
                set.remove(poll.value);
            }
        }, expirationTimeMillis, 1000, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
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

    public static void main(String[] args) {

    }
}