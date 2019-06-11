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
    // On concurrent `get` and `rotate`, it is acceptable to `get` the sample from an outdated `bucket`.
    return getCurrentBucket().get(q);
  }

  public void insert(double value) {
    // On concurrent `insert` and `rotate`, it might be acceptable to lose the measurement in the newest `bucket`.
    rotate();

    for (CKMSQuantiles bucket : buckets) {
      bucket.insert(value);
    }
  }

  private CKMSQuantiles getCurrentBucket() {
    rotate();

    return buckets.peek();
  }

  private void rotate() {
    // On concurrent `rotate` and `rotate`:
    //  - `currentTime` is cached to reduce thread contention.
    //  - `lastRotate` is used to ensure the correct number of rotations.
    long currentTime = System.currentTimeMillis();
    long lastRotate = lastRotateTimestampMillis.get();
    while (currentTime - lastRotate > durationBetweenRotatesMillis) {
      if (lastRotateTimestampMillis.compareAndSet(lastRotate, lastRotate + durationBetweenRotatesMillis)) {
        CKMSQuantiles bucket = new CKMSQuantiles(quantiles);
        // rotate buckets (not atomic)
        buckets.add(bucket);
        buckets.remove();
      }
      lastRotate = lastRotateTimestampMillis.get();
    }
  }

}
