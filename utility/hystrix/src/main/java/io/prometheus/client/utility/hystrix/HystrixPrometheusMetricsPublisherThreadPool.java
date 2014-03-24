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
 * <p>Implementation of {@link HystrixMetricsPublisherThreadPool} using the
 * <a href="https://github.com/prometheus/client_java">Prometheus Java Client</a>.</p>
 *
 * <p>This class is based on the <a href="https://github.com/Netflix/Hystrix/blob/master/hystrix-contrib/hystrix-codahale-metrics-publisher/src/main/java/com/netflix/hystrix/contrib/codahalemetricspublisher/HystrixCodaHaleMetricsPublisherThreadPool.java">HystrixCodaHaleMetricsPublisherThreadPool</a>.</p>
 */
public class HystrixPrometheusMetricsPublisherThreadPool
        implements HystrixMetricsPublisherThreadPool, ExpositionHook {

    private static final String SUBSYSTEM = "hystrix_thread_pool";
    private static final String POOL_NAME = "pool_name";

    private static final Map<String, Gauge> gauges = new ConcurrentHashMap<String, Gauge>();

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

        values.put(createMetricName("threadActiveCount"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getCurrentActiveCount();
            }
        });

        values.put(createMetricName("completedTaskCount"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getCurrentCompletedTaskCount();
            }
        });

        values.put(createMetricName("largestPoolSize"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getCurrentLargestPoolSize();
            }
        });

        values.put(createMetricName("totalTaskCount"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getCurrentTaskCount();
            }
        });

        values.put(createMetricName("queueSize"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getCurrentQueueSize();
            }
        });

        values.put(createMetricName("rollingMaxActiveThreads"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getRollingMaxActiveThreads();
            }
        });

        values.put(createMetricName("countThreadsExecuted"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getCumulativeCountThreadsExecuted();
            }
        });

        values.put(createMetricName("rollingCountThreadsExecuted"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getRollingCountThreadsExecuted();
            }
        });

        if (exportProperties) {
            values.put(createMetricName("propertyValue_corePoolSize"),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return properties.coreSize().get();
                    }
                }
            );
            values.put(createMetricName("propertyValue_keepAliveTimeInMinutes"),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return properties.keepAliveTimeMinutes().get();
                    }
                }
            );
            values.put(createMetricName("propertyValue_queueSizeRejectionThreshold"),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return properties.queueSizeRejectionThreshold().get();
                    }
                }
            );
            values.put(createMetricName("propertyValue_maxQueueSize"),
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

    private String createMetricName(String name) {
        String metricName = String.format("%s,%s,%s", namespace, SUBSYSTEM, name);
        if (!gauges.containsKey(metricName)) {
            String documentation = String.format(
                    "%s %s gauge partitioned by %s.",
                    SUBSYSTEM, name, POOL_NAME);
            gauges.put(metricName, Gauge.newBuilder()
                    .namespace(namespace)
                    .subsystem(SUBSYSTEM)
                    .name(name)
                    .labelNames(POOL_NAME)
                    .documentation(documentation)
                    .build());
        }
        return metricName;
    }
}
