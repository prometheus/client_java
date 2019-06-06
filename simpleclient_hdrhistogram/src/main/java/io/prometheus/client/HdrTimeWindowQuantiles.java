package io.prometheus.client;

import org.HdrHistogram.ConcurrentDoubleHistogram;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Wrapper around HdrHistogram.
 * <p>
 * Maintains a ring buffer of HdrHistogram to provide quantiles over a sliding windows of time.
 */
class HdrTimeWindowQuantiles {

  private final int numberOfSignificantValueDigits;
  private final ConcurrentLinkedQueue<ConcurrentDoubleHistogram> buckets;
  private final AtomicLong lastRotateTimestampMillis;
  private final long durationBetweenRotatesMillis;

  public HdrTimeWindowQuantiles(int numberOfSignificantValueDigits, long maxAgeSeconds, int ageBuckets) {
    this.numberOfSignificantValueDigits = numberOfSignificantValueDigits;
    this.buckets = new ConcurrentLinkedQueue<ConcurrentDoubleHistogram>();
    for (int i = 0; i < ageBuckets; i++) {
      this.buckets.add(new ConcurrentDoubleHistogram(numberOfSignificantValueDigits));
    }
    this.lastRotateTimestampMillis = new AtomicLong(System.currentTimeMillis());
    this.durationBetweenRotatesMillis = TimeUnit.SECONDS.toMillis(maxAgeSeconds) / ageBuckets;
  }

  public double get(double quantile) {
    // On concurrent `get` and `rotate`, it is acceptable to `get` the sample from an outdated `bucket`.
    ConcurrentDoubleHistogram currentBucket = getCurrentBucket();
    return currentBucket.getTotalCount() == 0 ? Double.NaN : currentBucket.getValueAtPercentile(quantile * 100.0);
  }

  public double getMin() {
    // On concurrent `get` and `rotate`, it is acceptable to `get` the sample from an outdated `bucket`.
    ConcurrentDoubleHistogram currentBucket = getCurrentBucket();
    return currentBucket.getTotalCount() == 0 ? Double.NaN : currentBucket.getMinValue();
  }

  public double getMax() {
    // On concurrent `get` and `rotate`, it is acceptable to `get` the sample from an outdated `bucket`.
    ConcurrentDoubleHistogram currentBucket = getCurrentBucket();
    return currentBucket.getTotalCount() == 0 ? Double.NaN : currentBucket.getMaxValue();
  }

  public void insert(double value) {
    // On concurrent `insert` and `rotate`, it might be acceptable to lose the measurement in the newest `bucket`.
    rotate();

    for (ConcurrentDoubleHistogram bucket : buckets) {
      bucket.recordValue(value);
    }
  }

  private ConcurrentDoubleHistogram getCurrentBucket() {
    // On concurrent `get` and `rotate`:
    //  - `currentBucket` could be `null` when there is only a single bucket (edge case).
    rotate();

    ConcurrentDoubleHistogram currentBucket;
    do {
      currentBucket = buckets.peek();
    } while (currentBucket == null);

    return currentBucket;
  }

  private void rotate() {
    // On concurrent `rotate` and `rotate`:
    //  - `currentTimeMillis` is cached to reduce thread contention.
    //  - `lastRotateTimestampMillis` is used to ensure the correct number of rotations.
    //  - `currentBucket` could be `null` when there is only a single bucket (edge case).
    long currentTimeMillis = System.currentTimeMillis();
    long lastRotateTimestampMillis = this.lastRotateTimestampMillis.get();
    ConcurrentDoubleHistogram currentBucket = buckets.peek();
    while (currentTimeMillis - lastRotateTimestampMillis > durationBetweenRotatesMillis) {
      ConcurrentDoubleHistogram bucket = new ConcurrentDoubleHistogram(numberOfSignificantValueDigits);
      if (this.lastRotateTimestampMillis.compareAndSet(
          lastRotateTimestampMillis, lastRotateTimestampMillis + durationBetweenRotatesMillis)
          && buckets.remove(currentBucket)) {
        buckets.add(bucket);
      }
      lastRotateTimestampMillis = this.lastRotateTimestampMillis.get();
      currentBucket = buckets.peek();
    }
  }

}
