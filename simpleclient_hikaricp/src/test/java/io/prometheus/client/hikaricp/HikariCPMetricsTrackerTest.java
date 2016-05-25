package io.prometheus.client.hikaricp;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLTransientConnectionException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;


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
    Connection connection = hikariDataSource.getConnection();
    try {
      hikariDataSource.getConnection();
    } catch (SQLTransientConnectionException e) {
    }
    connection.close();

    assertThat(CollectorRegistry.defaultRegistry.getSampleValue(
            "hikaricp_connection_timeout_count",
            labelNames,
            labelValues)
            .doubleValue(), is(1.0));
    assertThat(CollectorRegistry.defaultRegistry.getSampleValue(
            "hikaricp_connection_acquired_nanos_count",
            labelNames,
            labelValues)
            .doubleValue(), is(equalTo(1.0)));
    assertThat(CollectorRegistry.defaultRegistry.getSampleValue(
            "hikaricp_connection_acquired_nanos_sum",
            labelNames,
            labelValues)
            .doubleValue(), is(greaterThan(0.0)));
    assertThat(CollectorRegistry.defaultRegistry.getSampleValue(
            "hikaricp_connection_usage_millis_count",
            labelNames,
            labelValues)
            .doubleValue(), is(equalTo(1.0)));
    assertThat(CollectorRegistry.defaultRegistry.getSampleValue(
            "hikaricp_connection_usage_millis_sum",
            labelNames,
            labelValues)
            .doubleValue(), is(greaterThan(0.0)));
  }

}