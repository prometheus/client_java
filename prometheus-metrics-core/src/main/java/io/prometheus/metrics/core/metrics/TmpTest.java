package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.core.datapoints.Timer;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.Quantiles;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import io.prometheus.metrics.model.snapshots.Unit;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Collections;
import java.util.function.Consumer;

import static io.prometheus.metrics.model.snapshots.Unit.SECONDS;

public class TmpTest {

    static Consumer<CounterWithCallback.Callback> messageCountCallback = callback -> {
        double successfulMessageCount = getSuccessfulMessageCount();
        double erroneousMessageCount = getErroneousMessageCount();
        callback.call(successfulMessageCount, "success");
        callback.call(erroneousMessageCount, "error");
    };

    private static double getSuccessfulMessageCount() {
        return 2.0;
    }

    private static double getErroneousMessageCount() {
        return 8.0;
    }

    static Gauge currentActiveUsers = Gauge.newBuilder()
            .withName("current_active_users")
            .withHelp("Number of users that are currently active")
            .withLabelNames("region")
            .register();

    public static void login(String region) {
        currentActiveUsers.withLabelValues(region).inc();
        // perform login
    }

    public static void logout(String region) {
        currentActiveUsers.withLabelValues(region).dec();
        // perform logout
    }

    public static void main(String[] args) throws Exception {

        Histogram histogram = Histogram.newBuilder()
                .withName("http_request_duration_seconds")
                .withHelp("HTTP request service time in seconds")
                .withUnit(SECONDS)
                .withLabelNames("method", "path", "status_code")
                .register();

        long start = System.nanoTime();
        // do something
        histogram.withLabelValues("GET", "/", "200").observe(Unit.nanosToSeconds(System.nanoTime() - start));

        Histogram histogram2 = Histogram.newBuilder()
                .withName("request_duration_seconds")
                .withHelp("HTTP request service time in seconds")
                .withUnit(SECONDS)
                .withLabelNames("method", "path")
                .register();

        histogram2.withLabelValues("GET", "/").time(() -> {
            // sdflkj
        });


        login("us");
        login("us");
        logout("us");

        Gauge latestBatchJobDuration = Gauge.newBuilder()
                .withName("latest_backup_duration_seconds")
                .withHelp("Duration of the latest backup run in seconds.")
                .withUnit(SECONDS)
                .withLabelNames("env")
                .register();
        try (Timer timer = latestBatchJobDuration.withLabelValues("prod").startTimer()) {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        latestBatchJobDuration.withLabelValues("prod").time(() -> System.out.println("hi"));

        latestBatchJobDuration.withLabelValues("prod").time(() -> {
            return 1 + 2;
        });

        latestBatchJobDuration.withLabelValues("prod").timeChecked(() -> {
            return 1 + 2;
        });
        /*

        JVM_GC_COLLECTION_SECONDS,
              "Time spent in a given JVM garbage collector in seconds.",
              Collections.singletonList("gc"));
         */
        double MILLISECONDS_PER_SECOND = 1E3;

        SummaryWithCallback gcSeconds = SummaryWithCallback.newBuilder()
                .withName("jvm_gc_collection_seconds")
                .withHelp("Time spent in a given JVM garbage collector in seconds.")
                .withUnit(SECONDS)
                .withLabelNames("gc")
                .withCallback(callback -> {
                    for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
                        callback.call(
                                gc.getCollectionCount(),
                                gc.getCollectionTime() / MILLISECONDS_PER_SECOND,
                                Quantiles.EMPTY,
                                gc.getName()
                        );
                    }
                })
                .build();

        Runtime.getRuntime().gc();
        /*
        GaugeMetricFamily used = new GaugeMetricFamily(
                JVM_MEMORY_BYTES_USED,
                "Used bytes of a given JVM memory area.",
                Collections.singletonList("area"));
        used.addMetric(Collections.singletonList("heap"), heapUsage.getUsed());
        used.addMetric(Collections.singletonList("nonheap"), nonHeapUsage.getUsed());
        sampleFamilies.add(used);
         */

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        GaugeWithCallback memoryBytesUsed = GaugeWithCallback.newBuilder()
                .withName("jvm_memory_bytes_used")
                .withHelp("Used bytes of a given JVM memory area.")
                .withUnit(Unit.BYTES)
                .withLabelNames("area")
                .withCallback(callback -> {
                    callback.call(memoryBean.getHeapMemoryUsage().getUsed(), "heap");
                    callback.call(memoryBean.getNonHeapMemoryUsage().getUsed(), "nonheap");
                })
                .build();

        CounterWithCallback messageCount = CounterWithCallback.newBuilder()
                .withName("messages_total")
                .withLabelNames("status")
                .withConstLabels(Labels.of("staus", "b"))
                .withCallback(messageCountCallback)
                .build();

        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        CounterWithCallback counter = CounterWithCallback.newBuilder()
                .withName("classes_loaded_total")
                .withCallback(callback -> callback.call(classLoadingMXBean.getLoadedClassCount()))
                .build();
        CounterSnapshot sn = messageCount.collect();
        GaugeSnapshot sn2 = memoryBytesUsed.collect();
        SummarySnapshot sn3 = gcSeconds.collect();
        PrometheusRegistry r = PrometheusRegistry.defaultRegistry;
        MetricSnapshots s = r.scrape();
        System.out.println(sn2);
    }
}
