package io.prometheus.client.hikaricp;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.prometheus.client.CollectorRegistry;
import org.junit.Test;

import java.sql.SQLTransientConnectionException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HikariCPMetricsTrackerTest {
  @Test
  public void recordConnectionTimeout() throws Exception {
    String poolName = "record";

    HikariConfig config = new HikariConfig();
    config.setPoolName(poolName);
    config.setMetricsTrackerFactory(new HikariCPMetricsTrackerFactory());
    config.setJdbcUrl("jdbc:h2:mem:");
    config.setMaximumPoolSize(1);
    config.setConnectionTimeout(250);

    String[] labelNames = {"pool"};
    String[] labelValues = {poolName};

    HikariDataSource hikariDataSource = new HikariDataSource(config);
    hikariDataSource.getConnection();
    try {
      hikariDataSource.getConnection();
    } catch (SQLTransientConnectionException e) {
    }

    assertThat(CollectorRegistry.defaultRegistry.getSampleValue(
            "hikaricp_connection_timeout_count",
            labelNames,
            labelValues)
            .doubleValue(), is(1.0));
  }

}