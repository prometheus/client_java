package io.prometheus.client;

import io.prometheus.client.CKMSQuantiles.Quantile;
import io.prometheus.client.CKMSQuantilesWithExemplars.ValueWithExemplar;
import io.prometheus.client.exemplars.api.Exemplar;

import java.util.concurrent.TimeUnit;

/**
 * Copy of TimeWindowQuantiles but with Exemplars
 */
class TimeWindowQuantilesWithExemplars {

  private final Quantile[] quantiles;
  private final CKMSQuantilesWithExemplars[] ringBuffer;
  private int currentBucket;
  private long lastRotateTimestampMillis;
  private final long durationBetweenRotatesMillis;

  public TimeWindowQuantilesWithExemplars(Quantile[] quantiles, long maxAgeSeconds, int ageBuckets) {
    this.quantiles = quantiles;
    this.ringBuffer = new CKMSQuantilesWithExemplars[ageBuckets];
    for (int i = 0; i < ageBuckets; i++) {
      this.ringBuffer[i] = new CKMSQuantilesWithExemplars(quantiles);
    }
    this.currentBucket = 0;
    this.lastRotateTimestampMillis = System.currentTimeMillis();
    this.durationBetweenRotatesMillis = TimeUnit.SECONDS.toMillis(maxAgeSeconds) / ageBuckets;
  }

  public synchronized ValueWithExemplar get(double q) {
    CKMSQuantilesWithExemplars currentBucket = rotate();
    return currentBucket.get(q);
  }

  public synchronized void insert(double value, Exemplar exemplar) {
    rotate();
    for (CKMSQuantilesWithExemplars ckmsQuantiles : ringBuffer) {
      ckmsQuantiles.insert(value, exemplar);
    }
  }

  private CKMSQuantilesWithExemplars rotate() {
    long timeSinceLastRotateMillis = System.currentTimeMillis() - lastRotateTimestampMillis;
    while (timeSinceLastRotateMillis > durationBetweenRotatesMillis) {
      ringBuffer[currentBucket] = new CKMSQuantilesWithExemplars(quantiles);
      if (++currentBucket >= ringBuffer.length) {
        currentBucket = 0;
      }
      timeSinceLastRotateMillis -= durationBetweenRotatesMillis;
      lastRotateTimestampMillis += durationBetweenRotatesMillis;
    }
    return ringBuffer[currentBucket];
  }
}
