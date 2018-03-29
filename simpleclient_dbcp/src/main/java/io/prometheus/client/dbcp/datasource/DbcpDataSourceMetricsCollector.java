package io.prometheus.client.dbcp.datasource;


import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import org.apache.commons.dbcp.ManagedBasicDataSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Collect metrics from DBCP's ManagedBasicDataSource
 * from net.ju-n.commons-dbcp-jmx:commons-dbcp-jmx-jdbc(3 or 4).
 * It replaces commons-dbcp classes to have MBeans automatically registered:
 *
 * Original class	                                        Mbean enabled class
 * org.apache.commons.dbcp.BasicDataSource	        => org.apache.commons.dbcp.ManagedBasicDataSource
 * org.apache.commons.dbcp.BasicDataSourceFactory	=> org.apache.commons.dbcp.ManagedBasicDataSourceFactory
 *
 * <pre>{@code
 * public ManagedBasicDataSource buildDataSource(String type,
 *                                               String driverName,
 *                                               DataSourceProperties properties) throws DataSourceException {
 *     ManagedBasicDataSource dataSource = new ManagedBasicDataSource();
 *     dataSource.setDriverClassName(driverName);
 *
 *     dataSource.setTestWhileIdle(Boolean.TRUE);
 *     dataSource.setMaxActive(10);
 *
 *     dataSource.setUrl(String.format("jdbc:%s://%s:%s/%s", type, properties.getHost(), properties.getPort(), properties.getDatabase()));
 *     dataSource.setUsername(properties.getUser());
 *     dataSource.setPassword(properties.getPassword());
 *     return dataSource;
 * }}</pre>
 *
 * Then you can use it as following, considering multiple data sources
 *
 * <pre>{@code
 * DbcpDataSourceMetricsCollector dataSourceMetricsCollector = new DbcpDataSourceMetricsCollector().register()
 *
 * ManagedBasicDataSource masterDataSource = buildDataSource("postgresql", "org.postgresql.Driver", pgsqlProperties.getWrite());
 * dataSourceMetricsCollector.addDataSource("pgsql_write", masterDataSource);
 *
 * ManagedBasicDataSource slaveDataSource = buildDataSource("postgresql", "org.postgresql.Driver", postgresqlProperties.getRead());
 * dataSourceMetricsCollector.addDataSource("pgsql_read", slaveDataSource);
 * }</pre>
 *
 * Exposed metrics are labeled with the provided data source name.
 *
 * With the example above, sample metric names would be:
 * <pre>
 *     datasource_max_idle{datasource="pgsql_read"} 8.0
 *     datasource_max_idle{datasource="pgsql_write"} 8.0
 *     datasource_max_wait{datasource="pgsql_read"} -1.0
 *     datasource_max_wait{datasource="pgsql_write"} -1.0
 *     datasource_min_idle{datasource="pgsql_read"} 0.0
 *     datasource_min_idle{datasource="pgsql_write"} 0.0
 *     datasource_num_active{datasource="pgsql_read"} 0.0
 *     datasource_num_active{datasource="pgsql_write"} 0.0
 *     datasource_num_idle{datasource="pgsql_read"} 1.0
 *     datasource_num_idle{datasource="pgsql_write"} 0.0
 * </pre>
 *
 * @author Eduardo Mucelli Rezende Oliveira
 */

public class DbcpDataSourceMetricsCollector extends Collector {

    private final ConcurrentMap<String, ManagedBasicDataSource> children = new ConcurrentHashMap<String, ManagedBasicDataSource>();
    private List<String> labelNames = Collections.singletonList("datasource");

    public void addDataSource(String datasourceName, ManagedBasicDataSource dataSource) {
        children.put(datasourceName, dataSource);
    }

    public ManagedBasicDataSource removeDataSource(String dataSourceName) {
        return children.remove(dataSourceName);
    }

    public void clear() {
        children.clear();
    }

    @Override
    public List<MetricFamilySamples> collect() {
        GaugeMetricFamily maxActive = new GaugeMetricFamily("datasource_max_active",
                                                            "The maximum number of active connections that can be allocated at the same time.", labelNames);

        GaugeMetricFamily maxIdle = new GaugeMetricFamily("datasource_max_idle",
                                                          "The maximum number of connections that can remain idle in the pool.", labelNames);

        GaugeMetricFamily maxWait = new GaugeMetricFamily("datasource_max_wait",
                                                          "The maximum number of milliseconds that the pool will wait for a connection to be returned before throwing an exception.", labelNames);

        GaugeMetricFamily minIdle = new GaugeMetricFamily("datasource_min_idle",
                                                          "The minimum number of idle connections in the pool.", labelNames);

        GaugeMetricFamily numActive = new GaugeMetricFamily("datasource_num_active",
                                                            "The current number of active connections that have been allocated from this data source.", labelNames);

        GaugeMetricFamily numIdle = new GaugeMetricFamily("datasource_num_idle",
                                                            "The current number of idle connections that are waiting to be allocated from this data source.", labelNames);

        List<MetricFamilySamples> mfs = Arrays.<MetricFamilySamples>asList(maxActive, maxIdle, maxWait, minIdle, numActive, numIdle);

        for (Map.Entry<String, ManagedBasicDataSource> c : children.entrySet()) {
            List<String> dataSourceName = Collections.singletonList(c.getKey());
            ManagedBasicDataSource dataSource = c.getValue();

            maxActive.addMetric(dataSourceName, dataSource.getMaxActive());
            maxIdle.addMetric(dataSourceName, dataSource.getMaxIdle());
            maxWait.addMetric(dataSourceName, dataSource.getMaxWait());
            minIdle.addMetric(dataSourceName, dataSource.getMinIdle());
            numActive.addMetric(dataSourceName, dataSource.getNumActive());
            numIdle.addMetric(dataSourceName, dataSource.getNumIdle());
        }

        return mfs;
    }
}
