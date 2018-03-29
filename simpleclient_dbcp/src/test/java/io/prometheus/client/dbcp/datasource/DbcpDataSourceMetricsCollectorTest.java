package io.prometheus.client.dbcp.datasource;

import io.prometheus.client.CollectorRegistry;
import org.apache.commons.dbcp.ManagedBasicDataSource;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;


public class DbcpDataSourceMetricsCollectorTest {

    private CollectorRegistry registry;
    private ManagedBasicDataSource firstDataSource;
    private ManagedBasicDataSource secondDataSource;

    @Before
    public void setUp() throws Exception {
        registry = new CollectorRegistry();

        firstDataSource = new ManagedBasicDataSource();

        secondDataSource = new ManagedBasicDataSource();
        secondDataSource.setMaxActive(12);
        secondDataSource.setMaxIdle(10);
    }

    @Test
    public void testThatShouldPublishDataSourceMetrics() throws Exception {
        DbcpDataSourceMetricsCollector collector = new DbcpDataSourceMetricsCollector().register(registry);
        collector.addDataSource("main_source", firstDataSource);

        assertMetric(registry, "datasource_max_active", "main_source", 8);
        assertMetric(registry, "datasource_max_idle", "main_source", 8);
        assertMetric(registry, "datasource_max_wait", "main_source", -1);
        assertMetric(registry, "datasource_min_idle", "main_source", 0);
        assertMetric(registry, "datasource_num_active", "main_source", 0);
        assertMetric(registry, "datasource_num_idle", "main_source", 0);
    }

    @Test
    public void testThatItPublishesMetricsForMultipleDataSources() throws Exception {
        DbcpDataSourceMetricsCollector collector = new DbcpDataSourceMetricsCollector().register(registry);
        collector.addDataSource("first_source", firstDataSource);
        collector.addDataSource("second_source", secondDataSource);

        assertMetric(registry, "datasource_max_active", "first_source", 8);
        assertMetric(registry, "datasource_max_idle", "first_source", 8);
        assertMetric(registry, "datasource_max_wait", "first_source", -1);
        assertMetric(registry, "datasource_min_idle", "first_source", 0);
        assertMetric(registry, "datasource_num_active", "first_source", 0);
        assertMetric(registry, "datasource_num_idle", "first_source", 0);

        assertMetric(registry, "datasource_max_active", "second_source", 12);
        assertMetric(registry, "datasource_max_idle", "second_source", 10);
        assertMetric(registry, "datasource_max_wait", "second_source", -1);
        assertMetric(registry, "datasource_min_idle", "second_source", 0);
        assertMetric(registry, "datasource_num_active", "second_source", 0);
        assertMetric(registry, "datasource_num_idle", "second_source", 0);
    }

    private void assertMetric(CollectorRegistry registry, String name, String dataSourceName, double value) {
        assertThat(registry.getSampleValue(name, new String[]{"datasource"}, new String[]{dataSourceName})).isEqualTo(value);
    }

}