package io.prometheus.client.utility.hystrix;

import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolMetrics;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherThreadPool;
import io.prometheus.client.Prometheus;
import io.prometheus.client.Prometheus.ExpositionHook;
import io.prometheus.client.metrics.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Implementation of {@link HystrixMetricsPublisherThreadPool} using the <a href="https://github.com/prometheus/client_java">Prometheus Java Client</a>.</p>
 *
 * <p>This class is based on the <a href="https://github.com/Netflix/Hystrix/blob/master/hystrix-contrib/hystrix-codahale-metrics-publisher/src/main/java/com/netflix/hystrix/contrib/codahalemetricspublisher/HystrixCodaHaleMetricsPublisherThreadPool.java">HystrixCodaHaleMetricsPublisherThreadPool</a>.</p>
 *
 * <p>For a description of the hystrix metrics see the <a href="https://github.com/Netflix/Hystrix/wiki/Metrics-and-Monitoring">Hystrix Metrics &amp; Monitoring wiki</a>.<p/>
 */
public class HystrixPrometheusMetricsPublisherThreadPool
        implements HystrixMetricsPublisherThreadPool, ExpositionHook {

    private static final String SUBSYSTEM = "hystrix_thread_pool";
    private static final String POOL_NAME = "pool_name";

    private static final ConcurrentHashMap<String, Gauge> gauges = new ConcurrentHashMap<String, Gauge>();

    private final Map<String, Callable<Number>> values = new HashMap<String, Callable<Number>>();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String namespace;
    private final String poolName;
    private final boolean exportProperties;

    private final HystrixThreadPoolMetrics metrics;
    private final HystrixThreadPoolProperties properties;

    public HystrixPrometheusMetricsPublisherThreadPool(
            String namespace, HystrixThreadPoolKey key, HystrixThreadPoolMetrics metrics,
            HystrixThreadPoolProperties properties, boolean exportProperties) {

        this.namespace = namespace;
        this.poolName = key.name();
        this.exportProperties = exportProperties;

        this.metrics = metrics;
        this.properties = properties;
    }

    @Override
    public void initialize() {
        Prometheus.defaultAddPreexpositionHook(this);

        final String currentStateDoc = "Current state of thread-pool partitioned by pool_name.";

        values.put(createMetricName("thread_active_count", currentStateDoc),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getCurrentActiveCount();
                }
            }
        );
        values.put(createMetricName("completed_task_count", currentStateDoc),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getCurrentCompletedTaskCount();
                }
            }
        );
        values.put(createMetricName("largest_pool_size", currentStateDoc),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getCurrentLargestPoolSize();
                }
            }
        );
        values.put(createMetricName("total_task_count", currentStateDoc),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getCurrentTaskCount();
                }
            }
        );
        values.put(createMetricName("queue_size", currentStateDoc),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getCurrentQueueSize();
                }
            }
        );

        final String rollingCountDoc = "Rolling count partitioned by pool_name.";

        values.put(createMetricName("rolling_max_active_threads", rollingCountDoc),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getRollingMaxActiveThreads();
                }
            }
        );
        values.put(createMetricName("rolling_count_threads_executed", rollingCountDoc),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getRollingCountThreadsExecuted();
                }
            }
        );

        values.put(createMetricName("count_threads_executed",
            "Cumulative count partitioned by pool_name."),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getCumulativeCountThreadsExecuted();
                }
            }
        );



        if (exportProperties) {
            final String propDoc = "Configuration property partitioned by pool_name.";

            values.put(createMetricName("property_value_core_pool_size", propDoc),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return properties.coreSize().get();
                    }
                }
            );
            values.put(createMetricName("property_value_keep_alive_time_in_minutes", propDoc),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return properties.keepAliveTimeMinutes().get();
                    }
                }
            );
            values.put(createMetricName("property_value_queue_size_rejection_threshold", propDoc),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return properties.queueSizeRejectionThreshold().get();
                    }
                }
            );
            values.put(createMetricName("property_value_max_queue_size", propDoc),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return properties.maxQueueSize().get();
                    }
                }
            );
        }
    }

    @Override
    public void run() {
        for (Entry<String, Callable<Number>> metric : values.entrySet()) {
            try {
                double value = metric.getValue().call().doubleValue();
                gauges.get(metric.getKey())
                        .newPartial()
                        .labelPair(POOL_NAME, poolName)
                        .apply()
                        .set(value);
            } catch (Exception e) {
                logger.warn(String.format("Cannot export %s gauge for %s",
                        metric.getKey(), poolName), e);
            }
        }
    }

    private String createMetricName(String metric, String documentation) {
        String metricName = String.format("%s,%s,%s", namespace, SUBSYSTEM, metric);
        registerGauge(metricName, namespace, metric, documentation);
        return metricName;
    }

    /**
     * An instance of this class is created for each Hystrix thread-pool but our gauges are configured for
     * each metric within a given namespace. Although the {@link #initialize()} method is only called once
     * for each thread-pool by {@link com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherFactory}
     * in a thread-safe manner, this method will still be called more than once for each metric across
     * multiple threads so we should ensure that the gauge is only registered once.
     */
    private static void registerGauge(String metricName, String namespace,
                                      String metric, String documentation) {

        Gauge gauge = Gauge.newBuilder()
                .namespace(namespace)
                .subsystem(SUBSYSTEM)
                .name(metric)
                .labelNames(POOL_NAME)
                .documentation(documentation)
                .registerStatic(false)
                .build();

        Gauge existing = gauges.putIfAbsent(metricName, gauge);
        if (existing == null) {
            Prometheus.defaultRegister(gauge);
        }
    }
}
