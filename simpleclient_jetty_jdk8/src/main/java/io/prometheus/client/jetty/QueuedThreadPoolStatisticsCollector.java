package io.prometheus.client.jetty;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.GaugeMetricFamily;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class QueuedThreadPoolStatisticsCollector extends Collector {

  private static final List<String> LABEL_NAMES = Collections.singletonList("unit");

  private final Map<String, QueuedThreadPool> queuedThreadPoolMap = new ConcurrentHashMap<>();

  public QueuedThreadPoolStatisticsCollector() {
  }

  public QueuedThreadPoolStatisticsCollector(QueuedThreadPool queuedThreadPool, String name) {
    add(queuedThreadPool, name);
  }

  public QueuedThreadPoolStatisticsCollector add(QueuedThreadPool queuedThreadPool, String name) {
    queuedThreadPoolMap.put(name, queuedThreadPool);
    return this;
  }

  @Override
  public List<MetricFamilySamples> collect() {
    return Arrays.asList(
        buildGauge("jetty_queued_thread_pool_threads", "Number of total threads",
            QueuedThreadPool::getThreads),
        buildGauge("jetty_queued_thread_pool_threads_idle", "Number of idle threads",
            QueuedThreadPool::getIdleThreads),
        buildGauge("jetty_queued_thread_pool_threads_max", "Max size of thread pool",
            QueuedThreadPool::getMaxThreads),
        buildGauge("jetty_queued_thread_pool_jobs", "Number of total jobs",
            QueuedThreadPool::getQueueSize));
  }

  @Override
  public <T extends Collector> T register(CollectorRegistry registry) {
    if (queuedThreadPoolMap.isEmpty()) {
      throw new IllegalStateException("You must register at least one QueuedThreadPool.");
    }
    return super.register(registry);
  }

  private GaugeMetricFamily buildGauge(String metric, String help,
      Function<QueuedThreadPool, Integer> metricValueProvider) {
    final GaugeMetricFamily metricFamily = new GaugeMetricFamily(metric, help, LABEL_NAMES);
    queuedThreadPoolMap.forEach((key, value) -> metricFamily.addMetric(
        Collections.singletonList(key),
        metricValueProvider.apply(value)
    ));
    return metricFamily;
  }
}
