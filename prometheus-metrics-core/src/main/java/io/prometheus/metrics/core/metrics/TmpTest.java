package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.Quantiles;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import io.prometheus.metrics.model.snapshots.Unit;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Collections;
import java.util.function.Consumer;

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

    public static void main(String[] args) {

        /*

        JVM_GC_COLLECTION_SECONDS,
              "Time spent in a given JVM garbage collector in seconds.",
              Collections.singletonList("gc"));
         */
        double MILLISECONDS_PER_SECOND = 1E3;

        SummaryWithCallback gcSeconds = SummaryWithCallback.newBuilder()
                .withName("jvm_gc_collection_seconds")
                .withHelp("Time spent in a given JVM garbage collector in seconds.")
                .withUnit(Unit.SECONDS)
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
        System.out.println(sn2);
    }
}
