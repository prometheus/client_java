package io.prometheus.client.jetty;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.GaugeMetricFamily;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class QueuedThreadPoolStatisticsCollector extends Collector {

  private static final List<String> LABEL_NAMES = Collections.singletonList("unit");

  private final Map<String, QueuedThreadPool> queuedThreadPoolMap = new ConcurrentHashMap<String, QueuedThreadPool>();

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
    return Arrays.<MetricFamilySamples>asList(
        buildGauge("queued_thread_pool_threads", "Number of total threads",
            new ValueProvider() {
              @Override
              public double getValue(QueuedThreadPool queuedThreadPool) {
                return queuedThreadPool.getThreads();
              }
            }
        ),
        buildGauge("queued_thread_pool_idle_threads", "Number of idle threads",
            new ValueProvider() {
              @Override
              public double getValue(QueuedThreadPool queuedThreadPool) {
                return queuedThreadPool.getIdleThreads();
              }
            }
        )
    );
  }

  @Override
  public <T extends Collector> T register(CollectorRegistry registry) {
    if (queuedThreadPoolMap.isEmpty()) {
      throw new IllegalStateException("You must register at least one QueuedThreadPool.");
    }
    return super.register(registry);
  }

  private GaugeMetricFamily buildGauge(String metric, String help, ValueProvider valueProvider) {

    final GaugeMetricFamily metricFamily = new GaugeMetricFamily(metric, help, LABEL_NAMES);

    for (Entry<String, QueuedThreadPool> entry : queuedThreadPoolMap.entrySet()) {
      metricFamily.addMetric(
          Collections.singletonList(entry.getKey()),
          valueProvider.getValue(entry.getValue())
      );
    }
    return metricFamily;
  }

  private interface ValueProvider {

    double getValue(QueuedThreadPool queuedThreadPool);
  }
}
