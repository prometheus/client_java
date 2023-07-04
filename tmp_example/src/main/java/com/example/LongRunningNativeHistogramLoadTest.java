package com.example;

import io.prometheus.metrics.com_google_protobuf_3_21_7.TextFormat;
import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.expositionformats.PrometheusProtobufWriter;
import io.prometheus.metrics.expositionformats.generated.com_google_protobuf_3_21_7.Metrics;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LongRunningNativeHistogramLoadTest {

    private final int N_THREADS = 8;
    private final double[][] randoms = new double[N_THREADS][10 * 1024 * 1024];
    private Histogram prometheusHistogram;

    public static void main(String[] args) throws InterruptedException {
        new LongRunningNativeHistogramLoadTest().run();
    }

    public void run() throws InterruptedException {
        initRandomNumbers();
        initPrometheusHistogram();
        PrometheusRegistry registry = new PrometheusRegistry();
        registry.register(prometheusHistogram);
        ExecutorService producers = Executors.newFixedThreadPool(N_THREADS);
        ScheduledExecutorService scraper = Executors.newSingleThreadScheduledExecutor();

        PrometheusProtobufWriter protoWriter = new PrometheusProtobufWriter();
        OpenMetricsTextFormatWriter textWriter = new OpenMetricsTextFormatWriter(false, true);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        scraper.scheduleAtFixedRate(() -> {
            try {
                long start = System.nanoTime();
                MetricSnapshots snapshots = registry.scrape();
                Metrics.MetricFamily protobufData = protoWriter.convert(snapshots.get(0));
                System.out.println("# ---------------------------------");
                System.out.println("# " + dateFormat.format(Calendar.getInstance().getTime()));
                System.out.println("# ---------------------------------");
                textWriter.write(System.out, snapshots);
                System.out.println(TextFormat.printer().shortDebugString(protobufData));
                HistogramSnapshot.HistogramDataPointSnapshot data = ((HistogramSnapshot) snapshots.get(0)).getData().iterator().next();
                System.out.println("Schema: " + data.getNativeSchema());
                System.out.println("Number of buckets: " + data.getNativeBucketsForPositiveValues().size());
                long duration = System.nanoTime() - start;
                System.out.println("[" + Thread.currentThread().getName() + "] scraping took " + String.format("%.6f", ((double) duration) / (double) TimeUnit.SECONDS.toNanos(1)) + " s");
                System.out.println();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-2);
            }
        }, 1, 2, TimeUnit.MINUTES);

        // warmup
        CountDownLatch cdl = new CountDownLatch(N_THREADS);
        for (int thread = 0; thread < N_THREADS; thread++) {
            producers.submit(cdl::countDown);
        }
        cdl.await();

        for (int thread = 0; thread < N_THREADS; thread++) {
            int finalThread = thread;
            producers.submit(() -> {
                double[] random = randoms[finalThread];
                while (true) {
                    try {
                        long start = System.nanoTime();
                        for (int i = 0; i < random.length; i++) {
                            prometheusHistogram.observe(i);
                        }
                        long duration = System.nanoTime() - start;
                        System.out.println("[" + Thread.currentThread().getName() + "] observing took " + String.format("%.6f", ((double) duration) / (double) TimeUnit.SECONDS.toNanos(1)) + " s");
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                }
            });
        }
    }

    private void initPrometheusHistogram() {
        prometheusHistogram = Histogram.newBuilder()
                .withName("test")
                .withNativeResetDuration(60, TimeUnit.MINUTES)
                .build();
    }

    private void initRandomNumbers() {
        Random rand = new Random(0);
        for (int i = 0; i < N_THREADS; i++) {
            for (int j = 0; j < randoms[i].length; j++) {
                double observation = 0;
                while (observation < 0.13 || observation > 2.4) { // simulate observations around 180ms
                    observation = rand.nextGaussian() + 0.18;
                }
                randoms[i][j] = observation;
            }
        }
        System.out.println("random init done");
    }
}
