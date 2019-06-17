package io.prometheus.client;

import org.HdrHistogram.ConcurrentDoubleHistogram;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Wrapper around HdrHistogram.
 * <p>
 * Maintains a ring buffer of HdrHistogram to provide quantiles over a sliding windows of time.
 */
class HdrTimeWindowQuantiles {

  private final int numberOfSignificantValueDigits;
  private final AtomicReference<ConcurrentDoubleHistogram[]> buckets;
  private final AtomicLong lastRotateTimestampNanos;
  private final long durationBetweenRotatesNanos;

  public HdrTimeWindowQuantiles(int numberOfSignificantValueDigits, long maxAgeSeconds, int ageBuckets) {
    this.numberOfSignificantValueDigits = numberOfSignificantValueDigits;
    ConcurrentDoubleHistogram[] emptyBuckets = new ConcurrentDoubleHistogram[ageBuckets];
    for (int i = 0; i < ageBuckets; i++) {
      emptyBuckets[i] = new ConcurrentDoubleHistogram(numberOfSignificantValueDigits);
    }
    this.buckets = new AtomicReference<ConcurrentDoubleHistogram[]>(emptyBuckets);
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

    for (ConcurrentDoubleHistogram bucket : buckets.get()) {
      bucket.recordValue(value);
    }
  }

  private ConcurrentDoubleHistogram getCurrentBucket() {
    rotate();

    return buckets.get()[0]; // oldest bucket
  }

  private void rotate() {
    // On concurrent `rotate` and `rotate`:
    //  - `currentTime` is cached to reduce thread contention.
    //  - `lastRotate` is used to ensure the correct number of rotations.

    // Correctness is guaranteed by `volatile` memory access ordering and visibility semantics.
    // Note that it is not possible for other threads to read partially initialized `buckets`.
    // In other words the `volatile` write to `buckets` propagates preceding `plain` writes to `buckets[i]`.
    long currentTime = System.nanoTime();
    long lastRotate = lastRotateTimestampNanos.get();
    while (currentTime - lastRotate > durationBetweenRotatesNanos) {
      if (lastRotateTimestampNanos.compareAndSet(lastRotate, lastRotate + durationBetweenRotatesNanos)) {
        // rotate buckets (atomic)
        ConcurrentDoubleHistogram[] oldBuckets = buckets.get();
        int ageBuckets = oldBuckets.length;
        ConcurrentDoubleHistogram[] newBuckets = new ConcurrentDoubleHistogram[ageBuckets];
        newBuckets[ageBuckets - 1] = new ConcurrentDoubleHistogram(numberOfSignificantValueDigits); // newest bucket
        System.arraycopy(oldBuckets, 1, newBuckets, 0, ageBuckets - 1); // older buckets
        while (!buckets.compareAndSet(oldBuckets, newBuckets)) {
          oldBuckets = buckets.get();
          System.arraycopy(oldBuckets, 1, newBuckets, 0, ageBuckets - 1); // older buckets
        }
      }
      lastRotate = lastRotateTimestampNanos.get();
    }
  }

}
