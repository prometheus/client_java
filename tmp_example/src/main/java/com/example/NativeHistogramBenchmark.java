package com.example;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import io.prometheus.metrics.core.metrics.Histogram;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;

import java.io.IOException;
import java.util.Random;

@State(Scope.Benchmark)
public class NativeHistogramBenchmark {

    private final double[] random = new double[10 * 1024 * 1024];
    private Histogram prometheusHistogram;
    private DoubleHistogram otelHistogram;

    public static void main(String[] args) throws IOException {
        org.openjdk.jmh.Main.main(args);
    }

    @Setup
    public void setUp() {
        initPrometheusHistogram();
        initOTelHistogram();
        initRandomNumbers();
    }

    @Benchmark
    @Threads(1)
    public void benchmarkPrometheusHistogram_1_thread() {
        for (double observation : random) {
            prometheusHistogram.observe(observation);
        }
    }

    @Benchmark
    @Threads(1)
    public void benchmarkOtelHistogram_1_thread() {
        for (double observation : random) {
            otelHistogram.record(observation);
        }
    }

    @Benchmark
    @Threads(2)
    public void benchmarkPrometheusHistogram_2_threads() {
        for (double observation : random) {
            prometheusHistogram.observe(observation);
        }
    }

    @Benchmark
    @Threads(2)
    public void benchmarkOtelHistogram_2_threads() {
        for (double observation : random) {
            otelHistogram.record(observation);
        }
    }

    @Benchmark
    @Threads(4)
    public void benchmarkPrometheusHistogram_4_threads() {
        for (double observation : random) {
            prometheusHistogram.observe(observation);
        }
    }

    @Benchmark
    @Threads(4)
    public void benchmarkOtelHistogram_4_threads() {
        for (double observation : random) {
            otelHistogram.record(observation);
        }
    }

    @Benchmark
    @Threads(8)
    public void benchmarkPrometheusHistogram_8_threads() {
        for (double observation : random) {
            prometheusHistogram.observe(observation);
        }
    }

    @Benchmark
    @Threads(8)
    public void benchmarkOtelHistogram_8_threads() {
        for (double observation : random) {
            otelHistogram.record(observation);
        }
    }

    private void initPrometheusHistogram() {
        prometheusHistogram = Histogram.newBuilder().withName("test").nativeOnly().build();
    }

    private void initOTelHistogram() {

        // OTel SDK initialization, see https://opentelemetry.io/docs/instrumentation/java/manual/

        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "logical-service-name")));

        InMemoryMetricReader reader = InMemoryMetricReader.create();

        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                .registerMetricReader(reader) //OtlpGrpcMetricExporter.builder().build()).build())
                .setResource(resource)
                .registerView(InstrumentSelector.builder()
                                .setName("test.histogram")
                                .build(),
                        View.builder()
                                .setAggregation(Aggregation.base2ExponentialBucketHistogram(160, 5))
                                .build()
                )
                .build();

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                .setMeterProvider(sdkMeterProvider)
                .buildAndRegisterGlobal();

        Meter meter = openTelemetry.meterBuilder("instrumentation-library-name")
                .setInstrumentationVersion("1.0.0")
                .build();

        otelHistogram = meter.histogramBuilder("test.histogram")
                .build();
    }

    private void initRandomNumbers() {
        Random rand = new Random(0);
        for (int i = 0; i < random.length; i++) {
            random[i] = Math.abs(rand.nextGaussian());
        }
        System.out.println("random init done");
    }
}
