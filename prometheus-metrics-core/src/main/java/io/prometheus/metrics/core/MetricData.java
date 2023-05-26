package io.prometheus.metrics.core;

import io.prometheus.metrics.exemplars.DefaultExemplarSampler;
import io.prometheus.metrics.exemplars.ExemplarConfig;
import io.prometheus.metrics.exemplars.ExemplarSampler;
import io.prometheus.metrics.observer.Observer;

// package private
abstract class MetricData<T extends Observer> {

    protected volatile ExemplarSampler exemplarSampler;

    abstract T toObserver();

    // Some metrics might be statically initialized (static final Counter myCounter = ...).
    // However, some tracers (like Micrometer) may not be available at static initialization time.
    // Therefore, exemplarSampler must be lazily configured so that it will still be initialized if
    // a tracer is added later at runtime.
    // However, if no tracing is used or exemplars are disabled, this code should have almost zero overhead.
    // TODO: This is partly copy-and-paste
    protected void lazyInitExemplarSampler(ExemplarConfig config, Integer defaultNumberOfExemplars, double[] defaultBuckets) {
        if (exemplarSampler == null) {
            synchronized (this) {
                if (exemplarSampler == null) {
                    ExemplarConfig.Builder configBuilder;
                    if (config == null) {
                        configBuilder = ExemplarConfig.newBuilder();
                    } else {
                        configBuilder = config.toBuilder();
                    }
                    if (!configBuilder.hasBuckets() && !configBuilder.hasNumberOfExemplars()) {
                        if (defaultNumberOfExemplars != null) {
                            configBuilder.withNumberOfExemplars(defaultNumberOfExemplars);
                        } else if (defaultBuckets != null) {
                            configBuilder.withBuckets(defaultBuckets);
                        }
                        exemplarSampler = DefaultExemplarSampler.newInstance(configBuilder.build());
                    }
                }
            }
        }
    }
}
