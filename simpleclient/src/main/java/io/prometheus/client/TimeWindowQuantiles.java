package io.prometheus.client;

import io.prometheus.client.CKMSQuantiles.Quantile;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Wrapper around CKMSQuantiles.
 *
 * Maintains a ring buffer of CKMSQuantiles to provide quantiles over a sliding windows of time.
 */
class TimeWindowQuantiles {

  private final Quantile[] quantiles;
  private final ConcurrentLinkedQueue<CKMSQuantiles> buckets;
  private final AtomicLong lastRotateTimestampMillis;
  private final long durationBetweenRotatesMillis;

  public TimeWindowQuantiles(Quantile[] quantiles, long maxAgeSeconds, int ageBuckets) {
    this.quantiles = quantiles;
    this.buckets = new ConcurrentLinkedQueue<CKMSQuantiles>();
    for (int i = 0; i < ageBuckets; i++) {
      this.buckets.add(new CKMSQuantiles(quantiles));
    }
    this.lastRotateTimestampMillis = new AtomicLong(System.currentTimeMillis());
    this.durationBetweenRotatesMillis = TimeUnit.SECONDS.toMillis(maxAgeSeconds) / ageBuckets;
  }

  public double get(double q) {
    // On concurrent `get` and `rotate`:
    //  - it is acceptable to `get` the sample from an outdated `bucket`.
    //  - `currentBucket` could be `null` when there is only a single bucket (edge case).
    rotate();

    CKMSQuantiles currentBucket;
    do {
      currentBucket = buckets.peek();
    } while (currentBucket == null);

    return currentBucket.get(q);
  }

  public void insert(double value) {
    // On concurrent `insert` and `rotate`, it might be acceptable to lose the measurement in the newest `bucket`.
    rotate();

    for (CKMSQuantiles bucket : buckets) {
      bucket.insert(value);
    }
  }

  private void rotate() {
    // On concurrent `rotate` and `rotate`:
    //  - `currentTimeMillis` is cached to reduce thread contention.
    //  - `lastRotateTimestampMillis` is used to ensure the correct number of rotations.
    //  - `currentBucket` could be `null` when there is only a single bucket (edge case).
    long currentTimeMillis = System.currentTimeMillis();
    long lastRotateTimestampMillis = this.lastRotateTimestampMillis.get();
    CKMSQuantiles currentBucket = buckets.peek();
    while (currentTimeMillis - lastRotateTimestampMillis > durationBetweenRotatesMillis) {
      CKMSQuantiles bucket = new CKMSQuantiles(quantiles);
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
