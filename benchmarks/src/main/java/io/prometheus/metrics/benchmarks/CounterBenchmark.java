package io.prometheus.metrics.benchmarks;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.prometheus.metrics.core.datapoints.CounterDataPoint;
import io.prometheus.metrics.core.metrics.Counter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;

/**
 * Results on a machine with dedicated 8 vCPU cores:
 * <pre>
 * Benchmark                                                                  Mode  Cnt      Score     Error  Units
 * i.p.metrics.benchmarks.CounterBenchmark.codahaleIncNoLabels               thrpt   25  30978.055 ± 424.088  ops/s
 * i.p.metrics.benchmarks.CounterBenchmark.openTelemetryAdd                  thrpt   25  12682.744 ± 162.425  ops/s
 * i.p.metrics.benchmarks.CounterBenchmark.openTelemetryInc                  thrpt   25  14434.710 ±  99.809  ops/s
 * i.p.metrics.benchmarks.CounterBenchmark.openTelemetryIncNoLabels          thrpt   25  16634.416 ±  13.098  ops/s
 * i.p.metrics.benchmarks.CounterBenchmark.prometheusAdd                     thrpt   25  37317.024 ± 283.064  ops/s
 * i.p.metrics.benchmarks.CounterBenchmark.prometheusInc                     thrpt   25  39436.278 ± 458.583  ops/s
 * i.p.metrics.benchmarks.CounterBenchmark.prometheusNoLabelsInc             thrpt   25  34752.910 ± 293.979  ops/s
 * i.p.metrics.benchmarks.CounterBenchmark.simpleclientAdd                   thrpt   25   9520.592 ± 245.787  ops/s
 * i.p.metrics.benchmarks.CounterBenchmark.simpleclientInc                   thrpt   25   9057.637 ±  67.761  ops/s
 * i.p.metrics.benchmarks.CounterBenchmark.simpleclientNoLabelsInc           thrpt   25   8993.471 ±  49.581  ops/s
 * </pre>
 * Prometheus counters are faster than counters of other libraries. For example, incrementing a single counter
 * without labels is more than 2 times faster (34752 ops / second) than doing the same with an OpenTelemetry
 * counter (16634 ops / sec).
 */
public class CounterBenchmark {

    @State(Scope.Benchmark)
    public static class PrometheusCounter {

        final Counter noLabels;
        final CounterDataPoint dataPoint;

        public PrometheusCounter() {
            noLabels = Counter.builder()
                    .name("test")
                    .help("help")
                    .build();

            Counter labels = Counter.builder()
                    .name("test")
                    .help("help")
                    .labelNames("path", "status")
                    .build();
            this.dataPoint = labels.labelValues("/", "200");
        }
    }

    @State(Scope.Benchmark)
    public static class SimpleclientCounter {

        final io.prometheus.client.Counter noLabels;
        final io.prometheus.client.Counter.Child dataPoint;

        public SimpleclientCounter() {
            noLabels = io.prometheus.client.Counter.build()
                    .name("name")
                    .help("help")
                    .create();

            io.prometheus.client.Counter counter = io.prometheus.client.Counter.build()
                    .name("name")
                    .help("help")
                    .labelNames("path", "status")
                    .create();

            this.dataPoint = counter.labels("/", "200");
        }
    }

    @State(Scope.Benchmark)
    public static class CodahaleCounterNoLabels {
        final com.codahale.metrics.Counter counter = new com.codahale.metrics.MetricRegistry().counter("test");
    }

    @State(Scope.Benchmark)
    public static class OpenTelemetryCounter {

        final LongCounter longCounter;
        final DoubleCounter doubleCounter;
        final Attributes attributes;

        public OpenTelemetryCounter() {

            SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                    .registerMetricReader(InMemoryMetricReader.create())
                    .setResource(Resource.getDefault())
                    .build();
            OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                    .setMeterProvider(sdkMeterProvider)
                    .build();
            Meter meter = openTelemetry
                    .meterBuilder("instrumentation-library-name")
                    .setInstrumentationVersion("1.0.0")
                    .build();
            this.longCounter = meter
                    .counterBuilder("test1")
                    .setDescription("test")
                    .build();
            this.doubleCounter = meter
                    .counterBuilder("test2")
                    .ofDoubles()
                    .setDescription("test")
                    .build();
            this.attributes = Attributes.of(
                    AttributeKey.stringKey("path"), "/",
                    AttributeKey.stringKey("status"), "200");
        }
    }

    @Benchmark
    @Threads(4)
    public CounterDataPoint prometheusAdd(RandomNumbers randomNumbers, PrometheusCounter counter) {
        for (int i=0; i<randomNumbers.randomNumbers.length; i++) {
            counter.dataPoint.inc(randomNumbers.randomNumbers[i]);
        }
        return counter.dataPoint;
    }

    @Benchmark
    @Threads(4)
    public CounterDataPoint prometheusInc(PrometheusCounter counter) {
        for (int i=0; i<10*1024; i++) {
            counter.dataPoint.inc();
        }
        return counter.dataPoint;
    }

    @Benchmark
    @Threads(4)
    public DoubleCounter openTelemetryAdd(RandomNumbers randomNumbers, OpenTelemetryCounter counter) {
        for (int i=0; i<randomNumbers.randomNumbers.length; i++) {
            counter.doubleCounter.add(randomNumbers.randomNumbers[i], counter.attributes);
        }
        return counter.doubleCounter;
    }

    @Benchmark
    @Threads(4)
    public LongCounter openTelemetryInc(OpenTelemetryCounter counter) {
        for (int i=0; i<10*1024; i++) {
            counter.longCounter.add(1, counter.attributes);
        }
        return counter.longCounter;
    }

    @Benchmark
    @Threads(4)
    public LongCounter openTelemetryIncNoLabels(OpenTelemetryCounter counter) {
        for (int i=0; i<10*1024; i++) {
            counter.longCounter.add(1);
        }
        return counter.longCounter;
    }

    @Benchmark
    @Threads(4)
    public io.prometheus.client.Counter.Child simpleclientAdd(RandomNumbers randomNumbers, SimpleclientCounter counter) {
        for (int i=0; i<randomNumbers.randomNumbers.length; i++) {
            counter.dataPoint.inc(randomNumbers.randomNumbers[i]);
        }
        return counter.dataPoint;
    }

    @Benchmark
    @Threads(4)
    public io.prometheus.client.Counter.Child simpleclientInc(SimpleclientCounter counter) {
        for (int i=0; i<10*1024; i++) {
            counter.dataPoint.inc();
        }
        return counter.dataPoint;
    }

    @Benchmark
    @Threads(4)
    public com.codahale.metrics.Counter codahaleIncNoLabels(RandomNumbers randomNumbers, CodahaleCounterNoLabels counter) {
        for (int i=0; i<10*1024; i++) {
            counter.counter.inc();
        }
        return counter.counter;
    }

    @Benchmark
    @Threads(4)
    public io.prometheus.metrics.core.metrics.Counter prometheusNoLabelsInc(PrometheusCounter counter) {
        for (int i=0; i<10*1024; i++) {
            counter.noLabels.inc();
        }
        return counter.noLabels;
    }

    @Benchmark
    @Threads(4)
    public io.prometheus.client.Counter simpleclientNoLabelsInc(SimpleclientCounter counter) {
        for (int i=0; i<10*1024; i++) {
            counter.noLabels.inc();
        }
        return counter.noLabels;
    }
}
