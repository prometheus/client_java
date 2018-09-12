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
  private final boolean batchMode;

  public TimeWindowQuantiles(Quantile[] quantiles, long maxAgeSeconds, int ageBuckets) {
    this(quantiles, maxAgeSeconds, ageBuckets, false);
  }

  public TimeWindowQuantiles(Quantile[] quantiles, long maxAgeSeconds, int ageBuckets, boolean batchMode) {
    this.quantiles = quantiles;
    this.ringBuffer = new CKMSQuantiles[ageBuckets];
    for (int i = 0; i < ageBuckets; i++) {
      this.ringBuffer[i] = new CKMSQuantiles(quantiles);
    }
    this.batchMode = batchMode;
    this.currentBucket = 0;
    this.lastRotateTimestampMillis = batchMode ? 0 : System.currentTimeMillis();
    this.durationBetweenRotatesMillis = TimeUnit.SECONDS.toMillis(maxAgeSeconds) / ageBuckets;
  }

  // Do not rotate buffer in batch mode.
  public double get(double q) {
    CKMSQuantiles currentBucket = batchMode ? ringBuffer[this.currentBucket] : rotate(System.currentTimeMillis());
    return currentBucket.get(q);
  }

  public void insert(double value, long timestampMs) {
    rotate(timestampMs);
    for (CKMSQuantiles ckmsQuantiles : ringBuffer) {
      ckmsQuantiles.insert(value);
    }
  }

  public void insert(double value) {
    this.insert(value, System.currentTimeMillis());
  }

  private synchronized CKMSQuantiles rotate(long currentTimeMillis) {
    if (lastRotateTimestampMillis == 0) {
      lastRotateTimestampMillis = currentTimeMillis;
    }
    long timeSinceLastRotateMillis = currentTimeMillis - lastRotateTimestampMillis;
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
