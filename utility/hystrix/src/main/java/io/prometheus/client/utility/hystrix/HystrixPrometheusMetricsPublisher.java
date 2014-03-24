package io.prometheus.client.utility.hystrix;

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolMetrics;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherCommand;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherThreadPool;

/**
 * <p><a href="https://github.com/prometheus/client_java">Prometheus Java Client</a> implementation
 * of {@link HystrixMetricsPublisher}.</p>
 *
 * <p>This class is based on <a href="https://github.com/Netflix/Hystrix/blob/master/hystrix-contrib/hystrix-codahale-metrics-publisher/src/main/java/com/netflix/hystrix/contrib/codahalemetricspublisher/HystrixCodaHaleMetricsPublisher.java">HystrixCodaHaleMetricsPublisher</a>.</p>
 */
public class HystrixPrometheusMetricsPublisher extends HystrixMetricsPublisher {

    private final String namespace;
    private final boolean exportProperties;

    public HystrixPrometheusMetricsPublisher(String namespace, boolean exportProperties) {
        this.exportProperties = exportProperties;
        this.namespace = namespace;
    }

    @Override
    public HystrixMetricsPublisherCommand getMetricsPublisherForCommand(
            HystrixCommandKey commandKey, HystrixCommandGroupKey commandGroupKey,
            HystrixCommandMetrics metrics, HystrixCircuitBreaker circuitBreaker,
            HystrixCommandProperties properties) {

        return new HystrixPrometheusMetricsPublisherCommand(namespace, commandKey, commandGroupKey,
                metrics, circuitBreaker, properties, exportProperties);
    }

    @Override
    public HystrixMetricsPublisherThreadPool getMetricsPublisherForThreadPool(
            HystrixThreadPoolKey threadPoolKey, HystrixThreadPoolMetrics metrics,
            HystrixThreadPoolProperties properties) {

        return new HystrixPrometheusMetricsPublisherThreadPool(namespace, threadPoolKey,
                metrics, properties, exportProperties);
    }

    public static void register(String namespace) {
        HystrixPlugins.getInstance().registerMetricsPublisher(
            new HystrixPrometheusMetricsPublisher(namespace, false));
    }
}
