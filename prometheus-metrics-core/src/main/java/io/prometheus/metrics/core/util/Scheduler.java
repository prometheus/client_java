package io.prometheus.metrics.core.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class Scheduler {

    private static class DaemonThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        }
    }

    // TODO: Does this start a thread right away, or only after it is used for the first time?
    // If we want this library to work in native mode we can't start a thread in a static initializer.
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());

    public static ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return executor.schedule(command, delay, unit);
    }

    /**
     * For unit test. Wait until the executor Thread is running.
     */
    public static void awaitInitialization() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Scheduler.schedule(latch::countDown, 0, TimeUnit.MILLISECONDS);
        latch.await();
    }
}
