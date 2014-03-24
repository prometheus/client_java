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
 * <p>Implementation of {@link HystrixMetricsPublisherCommand} using the
 * <a href="https://github.com/prometheus/client_java">Prometheus Java Client</a>.</p>
 *
 * <p>This class is based on <a href="https://github.com/Netflix/Hystrix/blob/master/hystrix-contrib/hystrix-codahale-metrics-publisher/src/main/java/com/netflix/hystrix/contrib/codahalemetricspublisher/HystrixCodaHaleMetricsPublisherCommand.java">HystrixCodaHaleMetricsPublisherCommand</a>.</p>
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

        values.put(createMetricName("isCircuitBreakerOpen"), new Callable<Number>() {
            @Override
            public Number call() {
                return booleanToNumber(circuitBreaker.isOpen());
            }
        });

        // cumulative counts
        createCumulativeCountForEvent("countCollapsedRequests",
            HystrixRollingNumberEvent.COLLAPSED);
        createCumulativeCountForEvent("countExceptionsThrown",
            HystrixRollingNumberEvent.EXCEPTION_THROWN);
        createCumulativeCountForEvent("countFailure",
            HystrixRollingNumberEvent.FAILURE);
        createCumulativeCountForEvent("countFallbackFailure",
            HystrixRollingNumberEvent.FALLBACK_FAILURE);
        createCumulativeCountForEvent("countFallbackRejection",
            HystrixRollingNumberEvent.FALLBACK_REJECTION);
        createCumulativeCountForEvent("countFallbackSuccess",
            HystrixRollingNumberEvent.FALLBACK_SUCCESS);
        createCumulativeCountForEvent("countResponsesFromCache",
            HystrixRollingNumberEvent.RESPONSE_FROM_CACHE);
        createCumulativeCountForEvent("countSemaphoreRejected",
            HystrixRollingNumberEvent.SEMAPHORE_REJECTED);
        createCumulativeCountForEvent("countShortCircuited",
            HystrixRollingNumberEvent.SHORT_CIRCUITED);
        createCumulativeCountForEvent("countSuccess",
            HystrixRollingNumberEvent.SUCCESS);
        createCumulativeCountForEvent("countThreadPoolRejected",
            HystrixRollingNumberEvent.THREAD_POOL_REJECTED);
        createCumulativeCountForEvent("countTimeout",
            HystrixRollingNumberEvent.TIMEOUT);

        // rolling counts
        createRollingCountForEvent("rollingCountCollapsedRequests",
            HystrixRollingNumberEvent.COLLAPSED);
        createRollingCountForEvent("rollingCountExceptionsThrown",
            HystrixRollingNumberEvent.EXCEPTION_THROWN);
        createRollingCountForEvent("rollingCountFailure",
            HystrixRollingNumberEvent.FAILURE);
        createRollingCountForEvent("rollingCountFallbackFailure",
            HystrixRollingNumberEvent.FALLBACK_FAILURE);
        createRollingCountForEvent("rollingCountFallbackRejection",
            HystrixRollingNumberEvent.FALLBACK_REJECTION);
        createRollingCountForEvent("rollingCountFallbackSuccess",
            HystrixRollingNumberEvent.FALLBACK_SUCCESS);
        createRollingCountForEvent("rollingCountResponsesFromCache",
            HystrixRollingNumberEvent.RESPONSE_FROM_CACHE);
        createRollingCountForEvent("rollingCountSemaphoreRejected",
            HystrixRollingNumberEvent.SEMAPHORE_REJECTED);
        createRollingCountForEvent("rollingCountShortCircuited",
            HystrixRollingNumberEvent.SHORT_CIRCUITED);
        createRollingCountForEvent("rollingCountSuccess",
            HystrixRollingNumberEvent.SUCCESS);
        createRollingCountForEvent("rollingCountThreadPoolRejected",
            HystrixRollingNumberEvent.THREAD_POOL_REJECTED);
        createRollingCountForEvent("rollingCountTimeout",
            HystrixRollingNumberEvent.TIMEOUT);

        // the number of executionSemaphorePermits in use right now
        values.put(createMetricName("executionSemaphorePermitsInUse"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getCurrentConcurrentExecutionCount();
            }
        });

        // error percentage derived from current metrics
        values.put(createMetricName("errorPercentage"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getHealthCounts().getErrorPercentage();
            }
        });

        // latency metrics
        values.put(createMetricName("latencyExecute_mean"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getExecutionTimeMean();
            }
        });
        values.put(createMetricName("latencyExecute_percentile_5"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getExecutionTimePercentile(5);
            }
        });
        values.put(createMetricName("latencyExecute_percentile_25"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getExecutionTimePercentile(25);
            }
        });
        values.put(createMetricName("latencyExecute_percentile_50"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getExecutionTimePercentile(50);
            }
        });
        values.put(createMetricName("latencyExecute_percentile_75"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getExecutionTimePercentile(75);
            }
        });
        values.put(createMetricName("latencyExecute_percentile_90"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getExecutionTimePercentile(90);
            }
        });
        values.put(createMetricName("latencyExecute_percentile_99"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getExecutionTimePercentile(99);
            }
        });
        values.put(createMetricName("latencyExecute_percentile_995"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getExecutionTimePercentile(99.5);
            }
        });

        values.put(createMetricName("latencyTotal_mean"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getTotalTimeMean();
            }
        });
        values.put(createMetricName("latencyTotal_percentile_5"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getTotalTimePercentile(5);
            }
        });
        values.put(createMetricName("latencyTotal_percentile_25"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getTotalTimePercentile(25);
            }
        });
        values.put(createMetricName("latencyTotal_percentile_50"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getTotalTimePercentile(50);
            }
        });
        values.put(createMetricName("latencyTotal_percentile_75"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getTotalTimePercentile(75);
            }
        });
        values.put(createMetricName("latencyTotal_percentile_90"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getTotalTimePercentile(90);
            }
        });
        values.put(createMetricName("latencyTotal_percentile_99"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getTotalTimePercentile(99);
            }
        });
        values.put(createMetricName("latencyTotal_percentile_995"), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getTotalTimePercentile(99.5);
            }
        });

        if (exportProperties) {
            values.put(createMetricName("propertyValue_rollingStatisticalWindowInMilliseconds"),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return properties.metricsRollingStatisticalWindowInMilliseconds().get();
                    }
                }
            );
            values.put(createMetricName("propertyValue_circuitBreakerRequestVolumeThreshold"),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return properties.circuitBreakerRequestVolumeThreshold().get();
                    }
                }
            );
            values.put(createMetricName("propertyValue_circuitBreakerSleepWindowInMilliseconds"),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return properties.circuitBreakerSleepWindowInMilliseconds().get();
                    }
            });
            values.put(createMetricName("propertyValue_circuitBreakerErrorThresholdPercentage"),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return properties.circuitBreakerErrorThresholdPercentage().get();
                    }
                }
            );
            values.put(createMetricName("propertyValue_circuitBreakerForceOpen"),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return booleanToNumber(properties.circuitBreakerForceOpen().get());
                    }
                }
            );
            values.put(createMetricName("propertyValue_circuitBreakerForceClosed"),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return booleanToNumber(properties.circuitBreakerForceClosed().get());
                    }
                }
            );
            values.put(createMetricName(
                "propertyValue_executionIsolationThreadTimeoutInMilliseconds"),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return properties.executionIsolationThreadTimeoutInMilliseconds().get();
                    }
                }
            );
            values.put(createMetricName("propertyValue_executionIsolationStrategy"),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return properties.executionIsolationStrategy().get().ordinal();
                    }
                }
            );
            values.put(createMetricName("propertyValue_metricsRollingPercentileEnabled"),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return booleanToNumber(properties.metricsRollingPercentileEnabled().get());
                    }
                }
            );
            values.put(createMetricName("propertyValue_requestCacheEnabled"),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return booleanToNumber(properties.requestCacheEnabled().get());
                    }
                }
            );
            values.put(createMetricName("propertyValue_requestLogEnabled"),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return booleanToNumber(properties.requestLogEnabled().get());
                    }
                }
            );
            values.put(createMetricName(
                "propertyValue_executionIsolationSemaphoreMaxConcurrentRequests"),
                new Callable<Number>() {
                    @Override
                    public Number call() {
                        return properties.executionIsolationSemaphoreMaxConcurrentRequests().get();
                    }
                }
            );
            values.put(createMetricName(
                "propertyValue_fallbackIsolationSemaphoreMaxConcurrentRequests"),
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
        values.put(createMetricName(name), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getCumulativeCount(event);
            }
        });
    }

    private void createRollingCountForEvent(String name, final HystrixRollingNumberEvent event) {
        values.put(createMetricName(name), new Callable<Number>() {
            @Override
            public Number call() {
                return metrics.getRollingCount(event);
            }
        });
    }

    private int booleanToNumber(boolean value) {
        return value ? 1 : 0;
    }

    private String createMetricName(String name) {
        String metricName = String.format("%s,%s,%s", namespace, SUBSYSTEM, name);
        if (!gauges.containsKey(metricName)) {
            String documentation = String.format(
                    "%s %s gauge partitioned by %s and %s.",
                    SUBSYSTEM, name, COMMAND_GROUP, COMMAND_NAME);
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
