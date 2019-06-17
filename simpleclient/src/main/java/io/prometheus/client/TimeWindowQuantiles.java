package io.prometheus.client;

import io.prometheus.client.CKMSQuantiles.Quantile;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Wrapper around CKMSQuantiles.
 *
 * Maintains a ring buffer of CKMSQuantiles to provide quantiles over a sliding windows of time.
 */
class TimeWindowQuantiles {

  private final Quantile[] quantiles;
  private final AtomicReference<CKMSQuantiles[]> buckets;
  private final AtomicLong lastRotateTimestampNanos;
  private final long durationBetweenRotatesNanos;

  public TimeWindowQuantiles(Quantile[] quantiles, long maxAgeSeconds, int ageBuckets) {
    this.quantiles = quantiles;
    CKMSQuantiles[] emptyBuckets = new CKMSQuantiles[ageBuckets];
    for (int i = 0; i < ageBuckets; i++) {
      emptyBuckets[i] = new CKMSQuantiles(this.quantiles);
    }
    this.buckets = new AtomicReference<CKMSQuantiles[]>(emptyBuckets);
    this.lastRotateTimestampNanos = new AtomicLong(System.nanoTime());
    this.durationBetweenRotatesNanos = TimeUnit.SECONDS.toNanos(maxAgeSeconds) / ageBuckets;
  }

  public double get(double q) {
    // On concurrent `get` and `rotate`, it is acceptable to `get` the sample from an outdated `bucket`.
    return getCurrentBucket().get(q);
  }

  public void insert(double value) {
    // On concurrent `insert` and `rotate`, it should be acceptable to lose the measurement in the newest `bucket`.
    rotate();

    for (CKMSQuantiles bucket : buckets.get()) {
      bucket.insert(value);
    }
  }

  private CKMSQuantiles getCurrentBucket() {
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
        CKMSQuantiles[] oldBuckets = buckets.get();
        int ageBuckets = oldBuckets.length;
        CKMSQuantiles[] newBuckets = new CKMSQuantiles[ageBuckets];
        newBuckets[ageBuckets - 1] = new CKMSQuantiles(quantiles); // newest bucket
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
