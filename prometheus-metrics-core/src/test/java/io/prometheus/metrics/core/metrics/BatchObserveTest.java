package io.prometheus.metrics.core.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.prometheus.metrics.model.snapshots.ClassicHistogramBucket;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.NativeHistogramBucket;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

/** {@code observe(value, count)} must leave a histogram exactly as {@code count} single calls. */
class BatchObserveTest {

  private static Histogram hybrid(String name) {
    return Histogram.builder().name(name).nativeInitialSchema(5).build();
  }

  /**
   * Every integer-valued field of the data point. The sum is checked separately: a batch adds the
   * correctly rounded product, sequential observation accumulates one rounding per addition, so the
   * two agree to within a few ulps rather than bit for bit (see {@link #assertSameSum}).
   */
  private static String describe(HistogramSnapshot s) {
    HistogramSnapshot.HistogramDataPointSnapshot dp = s.getDataPoints().get(0);
    StringBuilder sb = new StringBuilder();
    sb.append("count=").append(dp.getCount());
    sb.append(" schema=").append(dp.getNativeSchema());
    sb.append(" zeroCount=").append(dp.getNativeZeroCount());
    sb.append(" zeroThreshold=").append(dp.getNativeZeroThreshold());
    sb.append(" classic=");
    for (ClassicHistogramBucket b : dp.getClassicBuckets()) {
      sb.append('[').append(b.getUpperBound()).append('=').append(b.getCount()).append(']');
    }
    sb.append(" pos=");
    for (NativeHistogramBucket b : dp.getNativeBucketsForPositiveValues()) {
      sb.append('[').append(b.getBucketIndex()).append('=').append(b.getCount()).append(']');
    }
    sb.append(" neg=");
    for (NativeHistogramBucket b : dp.getNativeBucketsForNegativeValues()) {
      sb.append('[').append(b.getBucketIndex()).append('=').append(b.getCount()).append(']');
    }
    return sb.toString();
  }

  private static void assertSameSum(HistogramSnapshot batch, HistogramSnapshot seq, long n) {
    double b = batch.getDataPoints().get(0).getSum();
    double s = seq.getDataPoints().get(0).getSum();
    if (Double.isInfinite(s) || s == 0.0) {
      assertThat(b).isEqualTo(s);
    } else {
      // Sequential accumulation carries at most one rounding error per addition.
      assertThat(b).isCloseTo(s, org.assertj.core.data.Offset.offset(Math.ulp(s) * n));
    }
  }

  @Test
  void batchMatchesSequentialForAssortedValues() {
    double[] values = {
      0.0,
      1e-9,
      -1e-9,
      0.5,
      1.0,
      1.5,
      3.7,
      -2.25,
      1e300,
      Double.POSITIVE_INFINITY,
      Double.NEGATIVE_INFINITY,
      0.25,
      1024.0
    };
    long[] counts = {1, 2, 7, 1000, 12345};
    for (double value : values) {
      for (long n : counts) {
        Histogram seq = hybrid("seq");
        Histogram batch = hybrid("batch");
        for (long i = 0; i < n; i++) {
          seq.observe(value);
        }
        batch.observe(value, n);
        HistogramSnapshot bs = batch.collect();
        HistogramSnapshot ss = seq.collect();
        assertThat(describe(bs)).as("value=%s n=%s", value, n).isEqualTo(describe(ss));
        assertSameSum(bs, ss, n);
      }
    }
  }

  @Test
  void sumIsTheCorrectlyRoundedProduct() {
    // 0.1 added ten times accumulates rounding error; the batch sum is the single correctly
    // rounded product. Both are legitimate; this pins down which one the batch produces.
    Histogram batch = hybrid("batch");
    batch.observe(0.1, 10);
    assertThat(batch.collect().getDataPoints().get(0).getSum()).isEqualTo(1.0);
    Histogram seq = hybrid("seq");
    for (int i = 0; i < 10; i++) {
      seq.observe(0.1);
    }
    assertThat(seq.collect().getDataPoints().get(0).getSum()).isEqualTo(0.9999999999999999);
  }

  @Test
  void batchMatchesSequentialAcrossScaleDown() {
    Histogram seq =
        Histogram.builder()
            .name("seq")
            .nativeOnly()
            .nativeInitialSchema(5)
            .nativeMaxNumberOfBuckets(8)
            .build();
    Histogram batch =
        Histogram.builder()
            .name("batch")
            .nativeOnly()
            .nativeInitialSchema(5)
            .nativeMaxNumberOfBuckets(8)
            .build();
    for (int i = 1; i <= 40; i++) {
      double v = i * 0.37;
      for (int k = 0; k < 3; k++) {
        seq.observe(v);
      }
      batch.observe(v, 3);
    }
    HistogramSnapshot bs = batch.collect();
    HistogramSnapshot ss = seq.collect();
    assertThat(describe(bs)).isEqualTo(describe(ss));
    assertSameSum(bs, ss, 120);
  }

  @Test
  void resetReappliesTheWholeBatch() throws Exception {
    // Scale the histogram down, then flag the reset duration as expired (as HistogramTest does for
    // the client_golang cases). The next observation resets the histogram and is re-applied; for a
    // batch, the whole batch must be re-applied.
    Histogram seq = scaledDown("seq");
    Histogram batch = scaledDown("batch");
    expireResetDuration(seq);
    expireResetDuration(batch);
    for (int k = 0; k < 9; k++) {
      seq.observe(2.5);
    }
    batch.observe(2.5, 9);
    HistogramSnapshot.HistogramDataPointSnapshot dp = batch.collect().getDataPoints().get(0);
    assertThat(dp.getNativeSchema()).isEqualTo(5); // reset restored the initial schema
    assertThat(dp.getCount()).isEqualTo(9);
    assertThat(dp.getSum()).isEqualTo(22.5);
    assertThat(describe(batch.collect())).isEqualTo(describe(seq.collect()));
  }

  private static Histogram scaledDown(String name) {
    Histogram h =
        Histogram.builder()
            .name(name)
            .nativeOnly()
            .nativeInitialSchema(5)
            .nativeMaxNumberOfBuckets(4)
            .build();
    for (int i = 1; i <= 64; i++) {
      h.observe(i * 0.61);
    }
    assertThat(h.collect().getDataPoints().get(0).getNativeSchema()).isLessThan(5);
    return h;
  }

  private static void expireResetDuration(Histogram h) throws Exception {
    Field flag = Histogram.DataPoint.class.getDeclaredField("resetDurationExpired");
    flag.setAccessible(true);
    flag.set(h.getNoLabels(), true);
  }

  @Test
  void zeroIsANoOpNegativeIsRejectedNaNIsIgnored() {
    Histogram h = hybrid("edge");
    h.observe(1.0, 0);
    assertThat(h.collect().getDataPoints().get(0).getCount()).isZero();
    assertThatThrownBy(() -> h.observe(1.0, -1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Negative count -1");
    h.observe(Double.NaN, 100);
    assertThat(h.collect().getDataPoints().get(0).getCount()).isZero();
    assertThat(h.collect().getDataPoints().get(0).getSum()).isZero();
  }

  @Test
  void interfaceDefaultLoops() {
    List<Double> seen = new ArrayList<>();
    io.prometheus.metrics.core.datapoints.DistributionDataPoint dp =
        new io.prometheus.metrics.core.datapoints.DistributionDataPoint() {
          @Override
          public long getCount() {
            return seen.size();
          }

          @Override
          public double getSum() {
            return 0;
          }

          @Override
          public void observe(double value) {
            seen.add(value);
          }

          @Override
          public void observeWithExemplar(
              double value, io.prometheus.metrics.model.snapshots.Labels labels) {
            seen.add(value);
          }
        };
    dp.observe(4.0, 3);
    assertThat(seen).containsExactly(4.0, 4.0, 4.0);
    assertThatThrownBy(() -> dp.observe(4.0, -2)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void batchesAreNeverSplitByConcurrentScrapes() throws Exception {
    Histogram h = hybrid("concurrent");
    int threads = 4;
    int iterations = 2000;
    long batchSize = 5;
    ExecutorService pool = Executors.newFixedThreadPool(threads + 1);
    CountDownLatch start = new CountDownLatch(1);
    AtomicReference<Throwable> failure = new AtomicReference<>();
    List<Future<?>> futures = new ArrayList<>();
    for (int t = 0; t < threads; t++) {
      futures.add(
          pool.submit(
              () -> {
                try {
                  start.await();
                  for (int i = 0; i < iterations; i++) {
                    h.observe(1.0 + (i % 7) * 0.25, batchSize);
                  }
                } catch (Throwable e) {
                  failure.compareAndSet(null, e);
                }
              }));
    }
    futures.add(
        pool.submit(
            () -> {
              try {
                start.await();
                for (int i = 0; i < 200; i++) {
                  HistogramSnapshot.HistogramDataPointSnapshot dp =
                      h.collect().getDataPoints().get(0);
                  // Every snapshot must contain whole batches only.
                  if (dp.getCount() % batchSize != 0) {
                    throw new AssertionError("torn batch: count=" + dp.getCount());
                  }
                }
              } catch (Throwable e) {
                failure.compareAndSet(null, e);
              }
            }));
    start.countDown();
    for (Future<?> f : futures) {
      f.get(120, TimeUnit.SECONDS);
    }
    pool.shutdown();
    assertThat(failure.get()).isNull();

    HistogramSnapshot.HistogramDataPointSnapshot dp = h.collect().getDataPoints().get(0);
    long expected = (long) threads * iterations * batchSize;
    assertThat(dp.getCount()).isEqualTo(expected);
    long classicTotal = 0;
    for (ClassicHistogramBucket b : dp.getClassicBuckets()) {
      classicTotal += b.getCount();
    }
    assertThat(classicTotal).isEqualTo(expected);
    long nativeTotal = dp.getNativeZeroCount();
    for (NativeHistogramBucket b : dp.getNativeBucketsForPositiveValues()) {
      nativeTotal += b.getCount();
    }
    for (NativeHistogramBucket b : dp.getNativeBucketsForNegativeValues()) {
      nativeTotal += b.getCount();
    }
    assertThat(nativeTotal).isEqualTo(expected);
  }

  @Test
  void summaryBatchMatchesSequential() {
    Summary seq = Summary.builder().name("seq").quantile(0.5).quantile(0.99).build();
    Summary batch = Summary.builder().name("batch").quantile(0.5).quantile(0.99).build();
    for (int i = 0; i < 100; i++) {
      for (int k = 0; k < 4; k++) {
        seq.observe(i);
      }
      batch.observe(i, 4);
    }
    SummarySnapshot.SummaryDataPointSnapshot s = seq.collect().getDataPoints().get(0);
    SummarySnapshot.SummaryDataPointSnapshot b = batch.collect().getDataPoints().get(0);
    assertThat(b.getCount()).isEqualTo(s.getCount());
    assertThat(b.getSum()).isEqualTo(s.getSum());
    assertThat(b.getQuantiles().get(0).getValue()).isEqualTo(s.getQuantiles().get(0).getValue());
    assertThat(b.getQuantiles().get(1).getValue()).isEqualTo(s.getQuantiles().get(1).getValue());

    Summary plain = Summary.builder().name("plain").build();
    plain.observe(0.5, 1_000_000_000L);
    SummarySnapshot.SummaryDataPointSnapshot p = plain.collect().getDataPoints().get(0);
    assertThat(p.getCount()).isEqualTo(1_000_000_000L);
    assertThat(p.getSum()).isEqualTo(5.0e8);
  }
}
