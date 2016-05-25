package io.prometheus.client.hikaricp;

import com.zaxxer.hikari.metrics.MetricsTracker;
import io.prometheus.client.Counter;
import io.prometheus.client.Summary;

public class HikariCPMetricsTracker extends MetricsTracker {
  private final Counter.Child counter;
  private final Summary.Child elapsedAcquiredSummary;
  private final Summary.Child elapsedBorrowedSummary;

  public HikariCPMetricsTracker(String poolName) {
    super();

    Counter counter = Counter.build()
            .name("hikaricp_connection_timeout_count")
            .labelNames("pool")
            .help("Connection timeout count")
            .register();

    this.counter = counter.labels(poolName);

    Summary elapsedAcquiredSummary = Summary.build()
            .name("hikaricp_connection_acquired_nanos")
            .labelNames("pool")
            .help("Connection acquired time")
            .register();
    this.elapsedAcquiredSummary = elapsedAcquiredSummary.labels(poolName);

    Summary elapsedBorrowedSummary = Summary.build()
            .name("hikaricp_connection_usage_millis")
            .labelNames("pool")
            .help("Connection usage")
            .register();
    this.elapsedBorrowedSummary = elapsedBorrowedSummary.labels(poolName);
  }

  @Override
  public void recordConnectionAcquiredNanos(long elapsedAcquiredNanos) {
    elapsedAcquiredSummary.observe(elapsedAcquiredNanos);
  }

  @Override
  public void recordConnectionUsageMillis(long elapsedBorrowedMillis) {
    elapsedBorrowedSummary.observe(elapsedBorrowedMillis);
  }

  @Override
  public void recordConnectionTimeout() {
    counter.inc();
  }
}
