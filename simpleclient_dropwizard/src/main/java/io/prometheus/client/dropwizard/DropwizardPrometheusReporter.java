package io.prometheus.client.dropwizard;

import java.io.IOException;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A reporter that publishes metrics to Prometheus Pushgateway.
 *
 * @see <a href="https://github.com/prometheus/pushgateway/">Prometheus Pushgateway</a>
 */
public class DropwizardPrometheusReporter extends ScheduledReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DropwizardPrometheusReporter.class);

    private final PrometheusSender sender;

    private DropwizardPrometheusReporter(MetricRegistry registry, MetricFilter filter, PrometheusSender sender) {
        super(registry, "prometheus-reporter", filter, TimeUnit.SECONDS, TimeUnit.SECONDS);
        this.sender = sender;
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        try {
            sender.send(new DropwizardExports(gauges, counters, histograms, meters, timers));
        } catch (IOException e) {
            LOGGER.warn("Failed to report: {}", e.getMessage());
        }
    }

    public static class Builder {

        private final MetricRegistry registry;
        private MetricFilter filter = MetricFilter.ALL;

        Builder(MetricRegistry registry) {
            this.registry = registry;
        }

        public Builder filter(MetricFilter filter) {
            if (filter == null) {
                throw new NullPointerException("filter must not be null");
            }
            this.filter = filter;
            return this;
        }

        public DropwizardPrometheusReporter build(PrometheusSender sender) {
            return new DropwizardPrometheusReporter(registry, filter, sender);
        }
    }

    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

}
