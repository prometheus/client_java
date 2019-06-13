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
  private final AtomicLong lastRotateTimestampNanos;
  private final long durationBetweenRotatesNanos;

  public HdrTimeWindowQuantiles(int numberOfSignificantValueDigits, long maxAgeSeconds, int ageBuckets) {
    this.numberOfSignificantValueDigits = numberOfSignificantValueDigits;
    this.buckets = new ConcurrentLinkedQueue<ConcurrentDoubleHistogram>();
    for (int i = 0; i < ageBuckets; i++) {
      this.buckets.add(new ConcurrentDoubleHistogram(numberOfSignificantValueDigits));
    }
    this.lastRotateTimestampNanos = new AtomicLong(System.nanoTime());
    this.durationBetweenRotatesNanos = TimeUnit.SECONDS.toNanos(maxAgeSeconds) / ageBuckets;
  }

  public double get(double quantile) {
    // On concurrent `get` and `rotate`, it is acceptable to `get` the sample from an outdated `bucket`.
    ConcurrentDoubleHistogram currentBucket = getCurrentBucket();
    return currentBucket.getTotalCount() == 0 ? Double.NaN : currentBucket.getValueAtPercentile(quantile * 100.0);
  }

  public double getMin() {
    ConcurrentDoubleHistogram currentBucket = getCurrentBucket();
    return currentBucket.getTotalCount() == 0 ? Double.NaN : currentBucket.getMinValue();
  }

  public double getMax() {
    ConcurrentDoubleHistogram currentBucket = getCurrentBucket();
    return currentBucket.getTotalCount() == 0 ? Double.NaN : currentBucket.getMaxValue();
  }

  public void insert(double value) {
    // On concurrent `insert` and `rotate`, it should be acceptable to lose the measurement in the newest `bucket`.
    rotate();

    for (ConcurrentDoubleHistogram bucket : buckets) {
      bucket.recordValue(value);
    }
  }

  private ConcurrentDoubleHistogram getCurrentBucket() {
    rotate();

    return buckets.peek();
  }

  private void rotate() {
    // On concurrent `rotate` and `rotate`:
    //  - `currentTime` is cached to reduce thread contention.
    //  - `lastRotate` is used to ensure the correct number of rotations.
    long currentTime = System.nanoTime();
    long lastRotate = lastRotateTimestampNanos.get();
    while (currentTime - lastRotate > durationBetweenRotatesNanos) {
      if (lastRotateTimestampNanos.compareAndSet(lastRotate, lastRotate + durationBetweenRotatesNanos)) {
        ConcurrentDoubleHistogram bucket = new ConcurrentDoubleHistogram(numberOfSignificantValueDigits);
        buckets.add(bucket);
        buckets.remove();
      }
      lastRotate = lastRotateTimestampNanos.get();
    }
  }

}
