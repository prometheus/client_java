---
title: JVM
weight: 1
---

The JVM instrumentation module provides a variety of out-of-the-box JVM and process metrics. To use it, add the following dependency:

{{< tabs "uniqueid" >}}
{{< tab "Gradle" >}}
```
implementation 'io.prometheus:prometheus-metrics-instrumentation-jvm:1.0.0'
```
{{< /tab >}}
{{< tab "Maven" >}}
```xml
<dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>prometheus-metrics-instrumentation-jvm</artifactId>
    <version>1.0.0</version>
</dependency>
```
{{< /tab >}}
{{< /tabs >}}

Now, you can register the JVM metrics as follows:

```java
JvmMetrics.builder().register();
```

The line above will initialize all JVM metrics and register them with the default registry. If you want to register the metrics with a custom `PrometheusRegistry`, you can pass the registry as parameter to the `register()` call.

The sections below describe the individual classes providing JVM metrics. If you don't want to register all JVM metrics, you can register each of these classes individually rather than using `JvmMetrics`.

JVM Buffer Pool Metrics
-----------------------

JVM buffer pool metrics are provided by the [JvmBufferPoolMetrics](/client_java/api/io/prometheus/metrics/instrumentation/jvm/JvmBufferPoolMetrics.html) class. The data is coming from the [BufferPoolMXBean](https://docs.oracle.com/en/java/javase/21/docs/api/java.management/java/lang/management/BufferPoolMXBean.html). Example metrics:

```
# HELP jvm_buffer_pool_capacity_bytes Bytes capacity of a given JVM buffer pool.
# TYPE jvm_buffer_pool_capacity_bytes gauge
jvm_buffer_pool_capacity_bytes{pool="direct"} 8192.0
jvm_buffer_pool_capacity_bytes{pool="mapped"} 0.0
# HELP jvm_buffer_pool_used_buffers Used buffers of a given JVM buffer pool.
# TYPE jvm_buffer_pool_used_buffers gauge
jvm_buffer_pool_used_buffers{pool="direct"} 1.0
jvm_buffer_pool_used_buffers{pool="mapped"} 0.0
# HELP jvm_buffer_pool_used_bytes Used bytes of a given JVM buffer pool.
# TYPE jvm_buffer_pool_used_bytes gauge
jvm_buffer_pool_used_bytes{pool="direct"} 8192.0
jvm_buffer_pool_used_bytes{pool="mapped"} 0.0
```

JVM Class Loading Metrics
-------------------------

JVM class loading metrics are provided by the [JvmClassLoadingMetrics](/client_java/api/io/prometheus/metrics/instrumentation/jvm/JvmClassLoadingMetrics.html) class. The data is coming from the [ClassLoadingMXBean](https://docs.oracle.com/en/java/javase/21/docs/api/java.management/java/lang/management/ClassLoadingMXBean.html). Example metrics:

```
# HELP jvm_classes_currently_loaded The number of classes that are currently loaded in the JVM
# TYPE jvm_classes_currently_loaded gauge
jvm_classes_currently_loaded 1109.0
# HELP jvm_classes_loaded_total The total number of classes that have been loaded since the JVM has started execution
# TYPE jvm_classes_loaded_total counter
jvm_classes_loaded_total 1109.0
# HELP jvm_classes_unloaded_total The total number of classes that have been unloaded since the JVM has started execution
# TYPE jvm_classes_unloaded_total counter
jvm_classes_unloaded_total 0.0
```

JVM Compilation Metrics
-----------------------

JVM compilation metrics are provided by the [JvmCompilationMetrics](/client_java/api/io/prometheus/metrics/instrumentation/jvm/JvmCompilationMetrics.html) class. The data is coming from the [CompilationMXBean](https://docs.oracle.com/en/java/javase/21/docs/api/java.management/java/lang/management/CompilationMXBean.html). Example metrics:

```
# HELP jvm_compilation_time_seconds_total The total time in seconds taken for HotSpot class compilation
# TYPE jvm_compilation_time_seconds_total counter
jvm_compilation_time_seconds_total 0.152
```

JVM Garbage Collector Metrics
-----------------------------

JVM garbage collector metrics are provided by the [JvmGarbageCollectorMetric](/client_java/api/io/prometheus/metrics/instrumentation/jvm/JvmGarbageCollectorMetrics.html) class. The data is coming from the [GarbageCollectorMXBean](https://docs.oracle.com/en/java/javase/21/docs/api/java.management/java/lang/management/GarbageCollectorMXBean.html). Example metrics:

```
# HELP jvm_gc_collection_seconds Time spent in a given JVM garbage collector in seconds.
# TYPE jvm_gc_collection_seconds summary
jvm_gc_collection_seconds_count{gc="PS MarkSweep"} 0
jvm_gc_collection_seconds_sum{gc="PS MarkSweep"} 0.0
jvm_gc_collection_seconds_count{gc="PS Scavenge"} 0
jvm_gc_collection_seconds_sum{gc="PS Scavenge"} 0.0
```

JVM Memory Metrics
------------------

JVM memory metrics are provided by the [JvmMemoryMetrics](/client_java/api/io/prometheus/metrics/instrumentation/jvm/JvmMemoryMetrics.html) class. The data is coming from the [MemoryMXBean](https://docs.oracle.com/en/java/javase/21/docs/api/java.management/java/lang/management/MemoryMXBean.html) and the [MemoryPoolMXBean](https://docs.oracle.com/en/java/javase/21/docs/api/java.management/java/lang/management/MemoryPoolMXBean.html). Example metrics:

```
# HELP jvm_memory_committed_bytes Committed (bytes) of a given JVM memory area.
# TYPE jvm_memory_committed_bytes gauge
jvm_memory_committed_bytes{area="heap"} 4.98597888E8
jvm_memory_committed_bytes{area="nonheap"} 1.1993088E7
# HELP jvm_memory_init_bytes Initial bytes of a given JVM memory area.
# TYPE jvm_memory_init_bytes gauge
jvm_memory_init_bytes{area="heap"} 5.20093696E8
jvm_memory_init_bytes{area="nonheap"} 2555904.0
# HELP jvm_memory_max_bytes Max (bytes) of a given JVM memory area.
# TYPE jvm_memory_max_bytes gauge
jvm_memory_max_bytes{area="heap"} 7.38983936E9
jvm_memory_max_bytes{area="nonheap"} -1.0
# HELP jvm_memory_objects_pending_finalization The number of objects waiting in the finalizer queue.
# TYPE jvm_memory_objects_pending_finalization gauge
jvm_memory_objects_pending_finalization 0.0
# HELP jvm_memory_pool_collection_committed_bytes Committed after last collection bytes of a given JVM memory pool.
# TYPE jvm_memory_pool_collection_committed_bytes gauge
jvm_memory_pool_collection_committed_bytes{pool="PS Eden Space"} 1.30023424E8
jvm_memory_pool_collection_committed_bytes{pool="PS Old Gen"} 3.47078656E8
jvm_memory_pool_collection_committed_bytes{pool="PS Survivor Space"} 2.1495808E7
# HELP jvm_memory_pool_collection_init_bytes Initial after last collection bytes of a given JVM memory pool.
# TYPE jvm_memory_pool_collection_init_bytes gauge
jvm_memory_pool_collection_init_bytes{pool="PS Eden Space"} 1.30023424E8
jvm_memory_pool_collection_init_bytes{pool="PS Old Gen"} 3.47078656E8
jvm_memory_pool_collection_init_bytes{pool="PS Survivor Space"} 2.1495808E7
# HELP jvm_memory_pool_collection_max_bytes Max bytes after last collection of a given JVM memory pool.
# TYPE jvm_memory_pool_collection_max_bytes gauge
jvm_memory_pool_collection_max_bytes{pool="PS Eden Space"} 2.727870464E9
jvm_memory_pool_collection_max_bytes{pool="PS Old Gen"} 5.542248448E9
jvm_memory_pool_collection_max_bytes{pool="PS Survivor Space"} 2.1495808E7
# HELP jvm_memory_pool_collection_used_bytes Used bytes after last collection of a given JVM memory pool.
# TYPE jvm_memory_pool_collection_used_bytes gauge
jvm_memory_pool_collection_used_bytes{pool="PS Eden Space"} 0.0
jvm_memory_pool_collection_used_bytes{pool="PS Old Gen"} 1249696.0
jvm_memory_pool_collection_used_bytes{pool="PS Survivor Space"} 0.0
# HELP jvm_memory_pool_committed_bytes Committed bytes of a given JVM memory pool.
# TYPE jvm_memory_pool_committed_bytes gauge
jvm_memory_pool_committed_bytes{pool="Code Cache"} 4128768.0
jvm_memory_pool_committed_bytes{pool="Compressed Class Space"} 917504.0
jvm_memory_pool_committed_bytes{pool="Metaspace"} 6946816.0
jvm_memory_pool_committed_bytes{pool="PS Eden Space"} 1.30023424E8
jvm_memory_pool_committed_bytes{pool="PS Old Gen"} 3.47078656E8
jvm_memory_pool_committed_bytes{pool="PS Survivor Space"} 2.1495808E7
# HELP jvm_memory_pool_init_bytes Initial bytes of a given JVM memory pool.
# TYPE jvm_memory_pool_init_bytes gauge
jvm_memory_pool_init_bytes{pool="Code Cache"} 2555904.0
jvm_memory_pool_init_bytes{pool="Compressed Class Space"} 0.0
jvm_memory_pool_init_bytes{pool="Metaspace"} 0.0
jvm_memory_pool_init_bytes{pool="PS Eden Space"} 1.30023424E8
jvm_memory_pool_init_bytes{pool="PS Old Gen"} 3.47078656E8
jvm_memory_pool_init_bytes{pool="PS Survivor Space"} 2.1495808E7
# HELP jvm_memory_pool_max_bytes Max bytes of a given JVM memory pool.
# TYPE jvm_memory_pool_max_bytes gauge
jvm_memory_pool_max_bytes{pool="Code Cache"} 2.5165824E8
jvm_memory_pool_max_bytes{pool="Compressed Class Space"} 1.073741824E9
jvm_memory_pool_max_bytes{pool="Metaspace"} -1.0
jvm_memory_pool_max_bytes{pool="PS Eden Space"} 2.727870464E9
jvm_memory_pool_max_bytes{pool="PS Old Gen"} 5.542248448E9
jvm_memory_pool_max_bytes{pool="PS Survivor Space"} 2.1495808E7
# HELP jvm_memory_pool_used_bytes Used bytes of a given JVM memory pool.
# TYPE jvm_memory_pool_used_bytes gauge
jvm_memory_pool_used_bytes{pool="Code Cache"} 4065472.0
jvm_memory_pool_used_bytes{pool="Compressed Class Space"} 766680.0
jvm_memory_pool_used_bytes{pool="Metaspace"} 6659432.0
jvm_memory_pool_used_bytes{pool="PS Eden Space"} 7801536.0
jvm_memory_pool_used_bytes{pool="PS Old Gen"} 1249696.0
jvm_memory_pool_used_bytes{pool="PS Survivor Space"} 0.0
# HELP jvm_memory_used_bytes Used bytes of a given JVM memory area.
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap"} 9051232.0
jvm_memory_used_bytes{area="nonheap"} 1.1490688E7
```

JVM Memory Pool Allocation Metrics
----------------------------------

JVM memory pool allocation metrics are provided by the [JvmMemoryPoolAllocationMetrics](/client_java/api/io/prometheus/metrics/instrumentation/jvm/JvmMemoryPoolAllocationMetrics.html) class. The data is obtained by adding a [NotificationListener](https://docs.oracle.com/en/java/javase/21/docs/api/java.management/javax/management/NotificationListener.html) to the [GarbageCollectorMXBean](https://docs.oracle.com/en/java/javase/21/docs/api/java.management/java/lang/management/GarbageCollectorMXBean.html). Example metrics:

```
# HELP jvm_memory_pool_allocated_bytes_total Total bytes allocated in a given JVM memory pool. Only updated after GC, not continuously.
# TYPE jvm_memory_pool_allocated_bytes_total counter
jvm_memory_pool_allocated_bytes_total{pool="Code Cache"} 4336448.0
jvm_memory_pool_allocated_bytes_total{pool="Compressed Class Space"} 875016.0
jvm_memory_pool_allocated_bytes_total{pool="Metaspace"} 7480456.0
jvm_memory_pool_allocated_bytes_total{pool="PS Eden Space"} 1.79232824E8
jvm_memory_pool_allocated_bytes_total{pool="PS Old Gen"} 1428888.0
jvm_memory_pool_allocated_bytes_total{pool="PS Survivor Space"} 4115280.0
```

JVM Runtime Info Metric
-----------------------


The JVM runtime info metric is provided by the [JvmRuntimeInfoMetric](/client_java/api/io/prometheus/metrics/instrumentation/jvm/JvmRuntimeInfoMetric.html) class. The data is obtained via system properties and will not change throughout the lifetime of the application. Example metric:

```
# TYPE jvm_runtime info
# HELP jvm_runtime JVM runtime info
jvm_runtime_info{runtime="OpenJDK Runtime Environment",vendor="Oracle Corporation",version="1.8.0_382-b05"} 1
```

JVM Thread Metrics
------------------

JVM thread metrics are provided by the [JvmThreadsMetrics](/client_java/api/io/prometheus/metrics/instrumentation/jvm/JvmThreadsMetrics.html) class. The data is coming from the [ThreadMXBean](https://docs.oracle.com/en/java/javase/21/docs/api/java.management/java/lang/management/ThreadMXBean.html). Example metrics:

```
# HELP jvm_threads_current Current thread count of a JVM
# TYPE jvm_threads_current gauge
jvm_threads_current 10.0
# HELP jvm_threads_daemon Daemon thread count of a JVM
# TYPE jvm_threads_daemon gauge
jvm_threads_daemon 8.0
# HELP jvm_threads_deadlocked Cycles of JVM-threads that are in deadlock waiting to acquire object monitors or ownable synchronizers
# TYPE jvm_threads_deadlocked gauge
jvm_threads_deadlocked 0.0
# HELP jvm_threads_deadlocked_monitor Cycles of JVM-threads that are in deadlock waiting to acquire object monitors
# TYPE jvm_threads_deadlocked_monitor gauge
jvm_threads_deadlocked_monitor 0.0
# HELP jvm_threads_peak Peak thread count of a JVM
# TYPE jvm_threads_peak gauge
jvm_threads_peak 10.0
# HELP jvm_threads_started_total Started thread count of a JVM
# TYPE jvm_threads_started_total counter
jvm_threads_started_total 10.0
# HELP jvm_threads_state Current count of threads by state
# TYPE jvm_threads_state gauge
jvm_threads_state{state="BLOCKED"} 0.0
jvm_threads_state{state="NEW"} 0.0
jvm_threads_state{state="RUNNABLE"} 5.0
jvm_threads_state{state="TERMINATED"} 0.0
jvm_threads_state{state="TIMED_WAITING"} 2.0
jvm_threads_state{state="UNKNOWN"} 0.0
jvm_threads_state{state="WAITING"} 3.0
```

Process Metrics
---------------

Process metrics are provided by the [ProcessMetrics](/client_java/api/io/prometheus/metrics/instrumentation/jvm/ProcessMetrics.html) class. The data is coming from the [OperatingSystemMXBean](https://docs.oracle.com/en/java/javase/21/docs/api/java.management/java/lang/management/OperatingSystemMXBean.html), the [RuntimeMXBean](https://docs.oracle.com/en/java/javase/21/docs/api/java.management/java/lang/management/RuntimeMXBean.html), and from the `/proc/self/status` file on Linux. The metrics with prefix `process_` are not specific to Java, but should be provided by every Prometheus client library, see [Process Metrics](https://prometheus.io/docs/instrumenting/writing_clientlibs/#process-metrics) in the Prometheus [writing client libraries](https://prometheus.io/docs/instrumenting/writing_clientlibs/#process-metrics) documentation. Example metrics:

```
# HELP process_cpu_seconds_total Total user and system CPU time spent in seconds.
# TYPE process_cpu_seconds_total counter
process_cpu_seconds_total 1.63
# HELP process_max_fds Maximum number of open file descriptors.
# TYPE process_max_fds gauge
process_max_fds 524288.0
# HELP process_open_fds Number of open file descriptors.
# TYPE process_open_fds gauge
process_open_fds 28.0
# HELP process_resident_memory_bytes Resident memory size in bytes.
# TYPE process_resident_memory_bytes gauge
process_resident_memory_bytes 7.8577664E7
# HELP process_start_time_seconds Start time of the process since unix epoch in seconds.
# TYPE process_start_time_seconds gauge
process_start_time_seconds 1.693829439767E9
# HELP process_virtual_memory_bytes Virtual memory size in bytes.
# TYPE process_virtual_memory_bytes gauge
process_virtual_memory_bytes 1.2683624448E10
```
