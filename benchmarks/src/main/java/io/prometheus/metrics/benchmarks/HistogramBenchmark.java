package io.prometheus.metrics.benchmarks;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.prometheus.metrics.core.metrics.Histogram;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;

import java.util.Arrays;

/**
 * Results on a machine with dedicated 8 vCPU cores:
 * <pre>
 * Benchmark                                                                  Mode  Cnt      Score     Error  Units
 * i.p.metrics.benchmarks.HistogramBenchmark.openTelemetryClassic            thrpt   25   1908.715 ± 114.050  ops/s
 * i.p.metrics.benchmarks.HistogramBenchmark.openTelemetryExponential        thrpt   25   1009.785 ±  12.965  ops/s
 * i.p.metrics.benchmarks.HistogramBenchmark.prometheusClassic               thrpt   25   6451.533 ± 326.265  ops/s
 * i.p.metrics.benchmarks.HistogramBenchmark.prometheusNative                thrpt   25   3372.789 ± 339.328  ops/s
 * i.p.metrics.benchmarks.HistogramBenchmark.simpleclient                    thrpt   25   6488.252 ±  96.737  ops/s
 * </pre>
 * The simpleclient (i.e. client_java version 0.16.0 and older) histograms perform about the same as
 * the classic histogram of the current 1.0.0 version.
 * <p>
 * Compared to OpenTelemetry histograms the Prometheus Java client histograms perform  more than 3 times better
 * (OpenTelemetry has 1908 ops / sec for classic histograms, while Prometheus has 6451 ops / sec).
 */

public class HistogramBenchmark {

    @State(Scope.Benchmark)
    public static class PrometheusClassicHistogram {

        final Histogram noLabels;

        public PrometheusClassicHistogram() {
            noLabels = Histogram.builder()
                    .name("test")
                    .help("help")
                    .classicOnly()
                    .build();
        }
    }

    @State(Scope.Benchmark)
    public static class PrometheusNativeHistogram {

        final Histogram noLabels;

        public PrometheusNativeHistogram() {
            noLabels = Histogram.builder()
                    .name("test")
                    .help("help")
                    .nativeOnly()
                    .nativeInitialSchema(5)
                    .nativeMaxNumberOfBuckets(0)
                    .build();
        }
    }

    @State(Scope.Benchmark)
    public static class SimpleclientHistogram {

        final io.prometheus.client.Histogram noLabels;

        public SimpleclientHistogram() {
            noLabels = io.prometheus.client.Histogram.build()
                    .name("name")
                    .help("help")
                    .create();
        }
    }

    @State(Scope.Benchmark)
    public static class OpenTelemetryClassicHistogram {

        final io.opentelemetry.api.metrics.DoubleHistogram histogram;

        public OpenTelemetryClassicHistogram() {

            SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                    .registerMetricReader(InMemoryMetricReader.create())
                    .setResource(Resource.getDefault())
                    .registerView(InstrumentSelector.builder()
                                    .setName("test")
                                    .build(),
                            View.builder()
                                    .setAggregation(Aggregation.explicitBucketHistogram(Arrays.asList(.005, .01, .025, .05, .1, .25, .5, 1.0, 2.5, 5.0, 10.0)))
                                    .build()
                    )
                    .build();
            OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                    .setMeterProvider(sdkMeterProvider)
                    .build();
            Meter meter = openTelemetry
                    .meterBuilder("instrumentation-library-name")
                    .setInstrumentationVersion("1.0.0")
                    .build();
            this.histogram = meter
                    .histogramBuilder("test")
                    .setDescription("test")
                    .build();
        }
    }

    @State(Scope.Benchmark)
    public static class OpenTelemetryExponentialHistogram {

        final io.opentelemetry.api.metrics.DoubleHistogram histogram;

        public OpenTelemetryExponentialHistogram() {

            SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                    .registerMetricReader(InMemoryMetricReader.create())
                    .setResource(Resource.getDefault())
                    .registerView(InstrumentSelector.builder()
                                    .setName("test")
                                    .build(),
                            View.builder()
                                    .setAggregation(Aggregation.base2ExponentialBucketHistogram(10_000, 5))
                                    .build()
                    )
                    .build();
            OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                    .setMeterProvider(sdkMeterProvider)
                    .build();
            Meter meter = openTelemetry
                    .meterBuilder("instrumentation-library-name")
                    .setInstrumentationVersion("1.0.0")
                    .build();
            this.histogram = meter
                    .histogramBuilder("test")
                    .setDescription("test")
                    .build();
        }
    }

    @Benchmark
    @Threads(4)
    public Histogram prometheusClassic(RandomNumbers randomNumbers, PrometheusClassicHistogram histogram) {
        for (int i = 0; i < randomNumbers.randomNumbers.length; i++) {
            histogram.noLabels.observe(randomNumbers.randomNumbers[i]);
        }
        return histogram.noLabels;
    }

    @Benchmark
    @Threads(4)
    public Histogram prometheusNative(RandomNumbers randomNumbers, PrometheusNativeHistogram histogram) {
        for (int i = 0; i < randomNumbers.randomNumbers.length; i++) {
            histogram.noLabels.observe(randomNumbers.randomNumbers[i]);
        }
        return histogram.noLabels;
    }

    @Benchmark
    @Threads(4)
    public io.prometheus.client.Histogram simpleclient(RandomNumbers randomNumbers, SimpleclientHistogram histogram) {
        for (int i = 0; i < randomNumbers.randomNumbers.length; i++) {
            histogram.noLabels.observe(randomNumbers.randomNumbers[i]);
        }
        return histogram.noLabels;
    }

    @Benchmark
    @Threads(4)
    public io.opentelemetry.api.metrics.DoubleHistogram openTelemetryClassic(RandomNumbers randomNumbers, OpenTelemetryClassicHistogram histogram) {
        for (int i = 0; i < randomNumbers.randomNumbers.length; i++) {
            histogram.histogram.record(randomNumbers.randomNumbers[i]);
        }
        return histogram.histogram;
    }

    @Benchmark
    @Threads(4)
    public io.opentelemetry.api.metrics.DoubleHistogram openTelemetryExponential(RandomNumbers randomNumbers, OpenTelemetryExponentialHistogram histogram) {
        for (int i = 0; i < randomNumbers.randomNumbers.length; i++) {
            histogram.histogram.record(randomNumbers.randomNumbers[i]);
        }
        return histogram.histogram;
    }
}
