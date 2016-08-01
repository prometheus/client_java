package io.prometheus.client;

import io.prometheus.client.CKMSQuantiles.Quantile;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper around CKMSQuantiles.
 *
 * Maintains a ring buffer of CKMSQuantiles to provide quantiles over a sliding windows of time.
 */
class TimeWindowQuantiles {

  private final Quantile[] quantiles;
  private final CKMSQuantiles[] ringBuffer;
  private int currentBucket;
  private long lastRotateTimestampMillis;
  private final long durationBetweenRotatesMillis;

  public TimeWindowQuantiles(Quantile[] quantiles, long maxAgeSeconds, int ageBuckets) {
    this.quantiles = quantiles;
    this.ringBuffer = new CKMSQuantiles[ageBuckets];
    for (int i = 0; i < ageBuckets; i++) {
      this.ringBuffer[i] = new CKMSQuantiles(quantiles);
    }
    this.currentBucket = 0;
    this.lastRotateTimestampMillis = System.currentTimeMillis();
    this.durationBetweenRotatesMillis = TimeUnit.SECONDS.toMillis(maxAgeSeconds) / ageBuckets;
  }

  public double get(double q) {
    CKMSQuantiles currentBucket = rotate();
    return currentBucket.get(q);
  }

  public void insert(double value) {
    rotate();
    for (int i=0; i<ringBuffer.length; i++) {
      ringBuffer[i].insert(value);
    }
  }

  private synchronized CKMSQuantiles rotate() {
    long timeSinceLastRotateMillis = System.currentTimeMillis() - lastRotateTimestampMillis;
    while (timeSinceLastRotateMillis > durationBetweenRotatesMillis) {
      ringBuffer[currentBucket] = new CKMSQuantiles(quantiles);
      if (++currentBucket >= ringBuffer.length) {
        currentBucket = 0;
      }
      timeSinceLastRotateMillis -= durationBetweenRotatesMillis;
      lastRotateTimestampMillis += durationBetweenRotatesMillis;
    }
    return ringBuffer[currentBucket];
  }
}
