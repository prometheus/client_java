package io.prometheus.client.hikaricp;

import com.zaxxer.hikari.metrics.MetricsTracker;
import com.zaxxer.hikari.metrics.MetricsTrackerFactory;
import com.zaxxer.hikari.metrics.PoolStats;

public class HikariCPMetricsTrackerFactory implements MetricsTrackerFactory {
  @Override
  public MetricsTracker create(String poolName, PoolStats poolStats) {
    new HikariCPCollector(poolName, poolStats).register();
    return new HikariCPMetricsTracker(poolName);
  }
}
