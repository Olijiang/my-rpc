package rpc.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Snion
 * @version 1.0
 * @description: TODO
 * @date 2024/6/6 22:04
 */
public class DaemonThreadFactory implements ThreadFactory {

    private final AtomicInteger threadNumber = new AtomicInteger(1);

    private final String threadName;

    public DaemonThreadFactory(String name) {
        this.threadName = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, threadName + "-" + threadNumber.getAndIncrement());
        if (!thread.isDaemon()) {
            thread.setDaemon(true);
        }
        return thread;
    }
}