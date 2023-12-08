package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CollectorBuilder {
    /**
     * Assembles collector from metric snapshot supplier.
     */
    public static Collector fromMetric(Supplier<MetricSnapshot> collector) {
        return new Collector() {
            @Override
            public MetricSnapshots collect(Predicate<String> includedNames, PrometheusScrapeRequest scrapeRequest) {
                MetricSnapshot result = collector.get();
                if (result.matches(includedNames))
                    return MetricSnapshots.of(result);
                else
                    return MetricSnapshots.empty();
            }
        };
    }

    /**
     * Assembles collector from metric snapshot supplier.
     */
    public static Collector fromMetrics(Supplier<MetricSnapshots> collector) {
        return (includedNames, scrapeRequest) -> collector.get().filter(includedNames);
    }

    /**
     * Applies name filter over result of another collector.
     * Useful if one suspects underlying collector does not follow filtering spec.
     * Required mainly for historical reasons.
     */
    public static Collector filtered(Collector collector) {
        return new Collector() {
            @Override
            public MetricSnapshots collect(Predicate<String> includedNames, PrometheusScrapeRequest scrapeRequest) {
                return collector.collect(includedNames, scrapeRequest).filter(includedNames);
            }
        };
    }

    /** Applies additional labels to all data points. */
    public static Collector withLabels(Collector collector, Labels labels) {
        return new Collector() {
            @Override
            public MetricSnapshots collect(Predicate<String> includedNames, PrometheusScrapeRequest scrapeRequest) {
                return collector.collect(includedNames, scrapeRequest).withLabels(labels);
            }
        };
    }

    /** Applies name prefix. */
    public static Collector withNamePrefix(Collector collector, String prefix) {
        return new Collector() {
            @Override
            public MetricSnapshots collect(Predicate<String> includedNames, PrometheusScrapeRequest scrapeRequest) {
                return collector.collect(name -> includedNames.test(prefix + name), scrapeRequest).withNamePrefix(prefix);
            }
        };
    }

    /** Constructs composite collector returning all the metrics from underling collectors. */
    public static Collector composite(Collector... collectors) {
        return new Collector() {
            @Override
            public MetricSnapshots collect(Predicate<String> includedNames, PrometheusScrapeRequest scrapeRequest) {
                MetricSnapshots.Builder result = MetricSnapshots.builder();
                for (Collector collector : collectors) {
                    result.metricSnapshots(collector.collect(includedNames, scrapeRequest));
                }
                return result.build();
            }
        };
    }

    public static CompositeCollector.Builder compositeBuilder() {
        return CompositeCollector.builder();
    }

    static class CompositeCollector implements Collector {
        private final Collection<Collector> collectors;

        private CompositeCollector(Collection<Collector> collectors) {
            this.collectors = collectors;
        }

        @Override
        public MetricSnapshots collect(Predicate<String> includedNames, PrometheusScrapeRequest scrapeRequest) {
            MetricSnapshots.Builder result = MetricSnapshots.builder();
            for (Collector collector : collectors) {
                result.metricSnapshots(collector.collect(includedNames, scrapeRequest));
            }
            return result.build();
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final List<Collector> collectors = new ArrayList<>();

            private Builder() {
            }

            public Builder add(Collector collector) {
                collectors.add(collector);
                return this;
            }

            public Builder add(Iterable<Collector> collectors) {
                collectors.forEach(Builder.this.collectors::add);
                return this;
            }

            public Collector build() {
                return new CompositeCollector(collectors);
            }
        }
    }
}
