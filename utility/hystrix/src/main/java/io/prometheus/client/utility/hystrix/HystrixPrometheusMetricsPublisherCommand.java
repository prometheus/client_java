package io.prometheus.client.utility.hystrix;

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherCommand;
import com.netflix.hystrix.util.HystrixRollingNumberEvent;
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
 * <p>Implementation of {@link HystrixMetricsPublisherCommand} using the <a href="https://github.com/prometheus/client_java">Prometheus Java Client</a>.</p>
 *
 * <p>This class is based on <a href="https://github.com/Netflix/Hystrix/blob/master/hystrix-contrib/hystrix-codahale-metrics-publisher/src/main/java/com/netflix/hystrix/contrib/codahalemetricspublisher/HystrixCodaHaleMetricsPublisherCommand.java">HystrixCodaHaleMetricsPublisherCommand</a>.</p>
 *
 * <p>For a description of the hystrix metrics see the <a href="https://github.com/Netflix/Hystrix/wiki/Metrics-and-Monitoring">Hystrix Metrics &amp; Monitoring wiki</a>.<p/>
 */
public class HystrixPrometheusMetricsPublisherCommand
        implements HystrixMetricsPublisherCommand, ExpositionHook {

    private static final String SUBSYSTEM = "hystrix_command";
    private static final String COMMAND_NAME = "command_name";
    private static final String COMMAND_GROUP = "command_group";

    private static final Map<String, Gauge> gauges = new ConcurrentHashMap<String, Gauge>();

    private final Map<String, Callable<Number>> values = new HashMap<String, Callable<Number>>();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String namespace;
    private final String commandName;
    private final String commandGroup;
    private final boolean exportProperties;

    private final HystrixCommandMetrics metrics;
    private final HystrixCircuitBreaker circuitBreaker;
    private final HystrixCommandProperties properties;

    public HystrixPrometheusMetricsPublisherCommand(
            String namespace, HystrixCommandKey commandKey, HystrixCommandGroupKey commandGroupKey,
            HystrixCommandMetrics metrics, HystrixCircuitBreaker circuitBreaker,
            HystrixCommandProperties properties, boolean exportProperties) {

        this.namespace = namespace;
        this.commandName = commandKey.name();
        this.commandGroup = (commandGroupKey != null) ? commandGroupKey.name() : "default";
        this.exportProperties = exportProperties;

        this.circuitBreaker = circuitBreaker;
        this.properties = properties;
        this.metrics = metrics;
    }

    @Override
    public void initialize() {
        Prometheus.defaultAddPreexpositionHook(this);

        values.put(createMetricName("is_circuit_breaker_open",
            "Current status of circuit breaker: 1 = open, 0 = closed."),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return booleanToNumber(circuitBreaker.isOpen());
                }
            }
        );
        values.put(createMetricName("execution_semaphore_permits_in_use",
            "The number of executionSemaphorePermits in use right now."),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getCurrentConcurrentExecutionCount();
                }
            }
        );
        values.put(createMetricName("error_percentage",
            "Error percentage derived from current metrics."),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getHealthCounts().getErrorPercentage();
                }
            }
        );

        createCumulativeCountForEvent("count_collapsed_requests",
            HystrixRollingNumberEvent.COLLAPSED);
        createCumulativeCountForEvent("count_exceptions_thrown",
            HystrixRollingNumberEvent.EXCEPTION_THROWN);
        createCumulativeCountForEvent("count_failure",
            HystrixRollingNumberEvent.FAILURE);
        createCumulativeCountForEvent("count_fallback_failure",
            HystrixRollingNumberEvent.FALLBACK_FAILURE);
        createCumulativeCountForEvent("count_fallback_rejection",
            HystrixRollingNumberEvent.FALLBACK_REJECTION);
        createCumulativeCountForEvent("count_fallback_success",
            HystrixRollingNumberEvent.FALLBACK_SUCCESS);
        createCumulativeCountForEvent("count_responses_from_cache",
            HystrixRollingNumberEvent.RESPONSE_FROM_CACHE);
        createCumulativeCountForEvent("count_semaphore_rejected",
            HystrixRollingNumberEvent.SEMAPHORE_REJECTED);
        createCumulativeCountForEvent("count_short_circuited",
            HystrixRollingNumberEvent.SHORT_CIRCUITED);
        createCumulativeCountForEvent("count_success",
            HystrixRollingNumberEvent.SUCCESS);
        createCumulativeCountForEvent("count_thread_pool_rejected",
            HystrixRollingNumberEvent.THREAD_POOL_REJECTED);
        createCumulativeCountForEvent("count_timeout",
            HystrixRollingNumberEvent.TIMEOUT);

        createRollingCountForEvent("rolling_count_collapsed_requests",
            HystrixRollingNumberEvent.COLLAPSED);
        createRollingCountForEvent("rolling_count_exceptions_thrown",
            HystrixRollingNumberEvent.EXCEPTION_THROWN);
        createRollingCountForEvent("rolling_count_failure",
            HystrixRollingNumberEvent.FAILURE);
        createRollingCountForEvent("rolling_count_fallback_failure",
            HystrixRollingNumberEvent.FALLBACK_FAILURE);
        createRollingCountForEvent("rolling_count_fallback_rejection",
            HystrixRollingNumberEvent.FALLBACK_REJECTION);
        createRollingCountForEvent("rolling_count_fallback_success",
            HystrixRollingNumberEvent.FALLBACK_SUCCESS);
        createRollingCountForEvent("rolling_count_responses_from_cache",
            HystrixRollingNumberEvent.RESPONSE_FROM_CACHE);
        createRollingCountForEvent("rolling_count_semaphore_rejected",
            HystrixRollingNumberEvent.SEMAPHORE_REJECTED);
        createRollingCountForEvent("rolling_count_short_circuited",
            HystrixRollingNumberEvent.SHORT_CIRCUITED);
        createRollingCountForEvent("rolling_count_success",
            HystrixRollingNumberEvent.SUCCESS);
        createRollingCountForEvent("rolling_count_thread_pool_rejected",
            HystrixRollingNumberEvent.THREAD_POOL_REJECTED);
        createRollingCountForEvent("rolling_count_timeout",
            HystrixRollingNumberEvent.TIMEOUT);

        final String latencyExecuteDescription = "Rolling percentiles of execution times for the "
            +"HystrixCommand.run() method (on the child thread if using thread isolation).";

        values.put(createMetricName("latency_execute_mean", latencyExecuteDescription),
            new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getExecutionTimeMean();
            }
        });
        values.put(createMetricName("latency_execute_percentile_5", latencyExecuteDescription),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getExecutionTimePercentile(5);
                }
            }
        );
        values.put(createMetricName("latency_execute_percentile_25", latencyExecuteDescription),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getExecutionTimePercentile(25);
                }
            }
        );
        values.put(createMetricName("latency_execute_percentile_50", latencyExecuteDescription),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getExecutionTimePercentile(50);
                }
            }
        );
        values.put(createMetricName("latency_execute_percentile_75", latencyExecuteDescription),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getExecutionTimePercentile(75);
                }
            }
        );
        values.put(createMetricName("latency_execute_percentile_90", latencyExecuteDescription),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getExecutionTimePercentile(90);
                }
            }
        );
        values.put(createMetricName("latency_execute_percentile_99", latencyExecuteDescription),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getExecutionTimePercentile(99);
                }
            }
        );
        values.put(createMetricName("latency_execute_percentile_995", latencyExecuteDescription),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getExecutionTimePercentile(99.5);
                }
            }
        );

        final String latencyTotalDescription = "Rolling percentiles of execution times for the "
            + "end-to-end execution of HystrixCommand.execute() or HystrixCommand.queue() until "
            + "a response is returned (or ready to return in case of queue(). The purpose of this "
            + "compared with the latency_execute* percentiles is to measure the cost of thread "
            + "queuing/scheduling/execution, semaphores, circuit breaker logic and other "
            + "aspects of overhead (including metrics capture itself).";

        values.put(createMetricName("latency_total_mean", latencyTotalDescription),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getTotalTimeMean();
                }
            }
        );
        values.put(createMetricName("latency_total_percentile_5", latencyTotalDescription),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getTotalTimePercentile(5);
                }
            }
        );
        values.put(createMetricName("latency_total_percentile_25", latencyTotalDescription),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getTotalTimePercentile(25);
                }
            }
        );
        values.put(createMetricName("latency_total_percentile_50", latencyTotalDescription),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getTotalTimePercentile(50);
                }
            }
        );
        values.put(createMetricName("latency_total_percentile_75", latencyTotalDescription),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getTotalTimePercentile(75);
                }
            }
        );
        values.put(createMetricName("latency_total_percentile_90", latencyTotalDescription),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getTotalTimePercentile(90);
                }
            }
        );
        values.put(createMetricName("latency_total_percentile_99", latencyTotalDescription),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getTotalTimePercentile(99);
                }
            }
        );
        values.put(createMetricName("latency_total_percentile_995", latencyTotalDescription),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getTotalTimePercentile(99.5);
                }
            }
        );

        if (exportProperties) {
            final String propertyValueDescription = "These informational metrics report the "
                + "actual property values being used by the HystrixCommand. This is useful to "
                + "see when a dynamic property takes effect and confirm a property is set as "
                + "expected.";

            values.put(createMetricName(
                "property_value_rolling_statistical_window_in_milliseconds",
                propertyValueDescription),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return properties.metricsRollingStatisticalWindowInMilliseconds().get();
                    }
                }
            );
            values.put(createMetricName(
                "property_value_circuit_breaker_request_volume_threshold",
                propertyValueDescription),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return properties.circuitBreakerRequestVolumeThreshold().get();
                    }
                }
            );
            values.put(createMetricName(
                "property_value_circuit_breaker_sleep_window_in_milliseconds",
                propertyValueDescription),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return properties.circuitBreakerSleepWindowInMilliseconds().get();
                    }
            });
            values.put(createMetricName(
                "property_value_circuit_breaker_error_threshold_percentage",
                propertyValueDescription),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return properties.circuitBreakerErrorThresholdPercentage().get();
                    }
                }
            );
            values.put(createMetricName(
                "property_value_circuit_breaker_force_open",
                propertyValueDescription),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return booleanToNumber(properties.circuitBreakerForceOpen().get());
                    }
                }
            );
            values.put(createMetricName(
                "property_value_circuit_breaker_force_closed",
                propertyValueDescription),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return booleanToNumber(properties.circuitBreakerForceClosed().get());
                    }
                }
            );
            values.put(createMetricName(
                "property_value_execution_isolation_thread_timeout_in_milliseconds",
                propertyValueDescription),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return properties.executionIsolationThreadTimeoutInMilliseconds().get();
                    }
                }
            );
            values.put(createMetricName(
                "property_value_execution_isolation_strategy",
                propertyValueDescription),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return properties.executionIsolationStrategy().get().ordinal();
                    }
                }
            );
            values.put(createMetricName(
                "property_value_metrics_rolling_percentile_enabled",
                propertyValueDescription),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return booleanToNumber(properties.metricsRollingPercentileEnabled().get());
                    }
                }
            );
            values.put(createMetricName(
                "property_value_request_cache_enabled",
                propertyValueDescription),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return booleanToNumber(properties.requestCacheEnabled().get());
                    }
                }
            );
            values.put(createMetricName(
                "property_value_request_log_enabled",
                propertyValueDescription),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return booleanToNumber(properties.requestLogEnabled().get());
                    }
                }
            );
            values.put(createMetricName(
                "property_value_execution_isolation_semaphore_max_concurrent_requests",
                propertyValueDescription),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return properties.executionIsolationSemaphoreMaxConcurrentRequests().get();
                    }
                }
            );
            values.put(createMetricName(
                "property_value_fallback_isolation_semaphore_max_concurrent_requests",
                propertyValueDescription),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return properties.fallbackIsolationSemaphoreMaxConcurrentRequests().get();
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
                        .labelPair(COMMAND_GROUP, commandGroup)
                        .labelPair(COMMAND_NAME, commandName)
                        .apply()
                        .set(value);
            } catch (Exception e) {
                logger.warn(String.format("Cannot export %s gauge for %s %s",
                        metric.getKey(), commandGroup, commandName), e);
            }
        }
    }

    private void createCumulativeCountForEvent(String name, final HystrixRollingNumberEvent event) {
        values.put(createMetricName(name,
            "These are cumulative counts since the start of the application."),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getCumulativeCount(event);
                }
            }
        );
    }

    private void createRollingCountForEvent(String name,  final HystrixRollingNumberEvent event) {
        values.put(createMetricName(name,
            "These are \"point in time\" counts representing the last X seconds."),
            new Callable<Number>() {
                @Override
                public Number call() {
                    return metrics.getRollingCount(event);
                }
            }
        );
    }

    private int booleanToNumber(boolean value) {
        return value ? 1 : 0;
    }

    private String createMetricName(String name, String documentation) {
        String metricName = String.format("%s,%s,%s", namespace, SUBSYSTEM, name);
        if (!gauges.containsKey(metricName)) {
            gauges.put(metricName, Gauge.newBuilder()
                    .namespace(namespace)
                    .subsystem(SUBSYSTEM)
                    .name(name)
                    .labelNames(COMMAND_GROUP, COMMAND_NAME)
                    .documentation(documentation)
                    .build());
        }
        return metricName;
    }
}
