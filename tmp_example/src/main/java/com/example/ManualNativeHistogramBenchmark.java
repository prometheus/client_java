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

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ManualNativeHistogramBenchmark {


    private final int MAX_THREADS = 32;
    private final double[][] randoms = new double[MAX_THREADS][10 * 1024 * 1024];
    private Meter meter;
    private Histogram prometheusHistogram;
    private DoubleHistogram otelHistogram;

    public static void main(String[] args) throws InterruptedException {
        ManualNativeHistogramBenchmark benchmark = new ManualNativeHistogramBenchmark();
        benchmark.setUp();
        for (int threads = 1; threads <= 48; threads++) {
            System.out.println();
            System.out.println("# " + threads + " Threads");
            System.out.println();
            benchmark.run(threads);
        }
    }

    private void runParallel(String msg, Consumer<Integer> func, ExecutorService executorService, int nThreads) throws InterruptedException {
        long start = System.nanoTime();
        CountDownLatch cdl = new CountDownLatch(nThreads);
        for (int thread = 0; thread < nThreads; thread++) {
            int finalThread = thread;
            executorService.submit(() -> {
                func.accept(finalThread);
                cdl.countDown();
            });
        }
        cdl.await();
        long duration = System.nanoTime() - start;
        System.out.println(msg + " took " + String.format("%.6f", ((double) duration) / (double) TimeUnit.SECONDS.toNanos(1)) + " s");
    }

    public void run(int nThreads) throws InterruptedException {
        if (nThreads > MAX_THREADS) {
            throw new IllegalStateException();
        }
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        CountDownLatch countDownLatch = new CountDownLatch(nThreads);
        for (int i = 0; i < nThreads; i++) {
            executorService.submit(countDownLatch::countDown);
        }
        countDownLatch.await();

        int nWarmupIterations = 5;
        int nRealIterations = 8;

        // prometheus
        for (int i = 0; i < nWarmupIterations; i++) {
            runParallel("Prometheus warmup iteration " + (i + 1) + " of " + nWarmupIterations, this::runPrometheusBenchmark, executorService, nThreads);
            initPrometheusHistogram();
        }
        for (int i = 0; i < nRealIterations; i++) {
            runParallel("Prometheus iteration " + (i + 1) + " of " + nRealIterations, this::runPrometheusBenchmark, executorService, nThreads);
            initPrometheusHistogram();
        }

        // otel
        for (int i = 0; i < nWarmupIterations; i++) {
            runParallel("OTel warmup iteration " + (i + 1) + " of " + nWarmupIterations, this::runOTelBenchmark, executorService, nThreads);
            initPrometheusHistogram();
        }
        for (int i = 0; i < nRealIterations; i++) {
            runParallel("OTel iteration " + (i + 1) + " of " + nRealIterations, this::runOTelBenchmark, executorService, nThreads);
            initPrometheusHistogram();
        }
    }


    public void runPrometheusBenchmark(int threadNumber) {
        try {
            for (double observation : randoms[threadNumber]) {
                prometheusHistogram.observe(observation);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void runOTelBenchmark(int threadNumber) {
        try {
            for (double observation : randoms[threadNumber]) {
                otelHistogram.record(observation);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void setUp() {
        initPrometheusHistogram();
        initOTelMeter();
        initOTelHistogram();
        initRandomNumbers();
    }

    private void initPrometheusHistogram() {
        prometheusHistogram = Histogram.newBuilder().withName("test").nativeOnly().build();
    }

    private void initOTelMeter() {

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

        meter = openTelemetry.meterBuilder("instrumentation-library-name")
                .setInstrumentationVersion("1.0.0")
                .build();
    }

    private void initOTelHistogram() {
        otelHistogram = meter.histogramBuilder("test.histogram")
                .build();
    }

    private void initRandomNumbers() {
        Random rand = new Random(0);
        for (int i = 0; i < MAX_THREADS; i++) {
            for (int j = 0; j < randoms[i].length; j++) {
                randoms[i][j] = Math.abs(rand.nextGaussian());
            }
        }
        System.out.println("random init done");
    }
}
