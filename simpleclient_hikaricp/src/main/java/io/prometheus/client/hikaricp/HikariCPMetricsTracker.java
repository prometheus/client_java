package io.prometheus.client.hikaricp;

import com.zaxxer.hikari.metrics.MetricsTracker;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;

public class HikariCPMetricsTracker extends MetricsTracker {
  private final Counter.Child counter;

  public HikariCPMetricsTracker(String poolName) {
    super();

    Counter counter = Counter.build()
            .name("hikaricp_connection_timeout_count")
            .labelNames("pool")
            .help("Connection timeout count")
            .register();

    this.counter = counter.labels(poolName);
  }

  @Override
  public void recordConnectionAcquiredNanos(long elapsedAcquiredNanos) {
    super.recordConnectionAcquiredNanos(elapsedAcquiredNanos);
  }

  @Override
  public void recordConnectionUsageMillis(long elapsedBorrowedMillis) {
    super.recordConnectionUsageMillis(elapsedBorrowedMillis);
  }

  @Override
  public void recordConnectionTimeout() {
    super.recordConnectionTimeout();
    counter.inc();
  }
}
