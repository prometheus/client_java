package io.prometheus.client.hibernate;

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

/**
 * Collect metrics from Hibernate statistics.
 * <p>
 * Usage example:
 * <pre>
 * new HibernateStatisticsCollector(sessionFactory, "default").register();
 * </pre>
 * If you are in a JPA environment, you can obtain the SessionFactory like this:
 * <pre>
 * SessionFactory sessionFactory =
 *     entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
 * </pre>
 *
 * @author Christian Kaltepoth
 */
public class HibernateStatisticsCollector extends Collector {

  private static final List<String> LABEL_NAMES = Collections.singletonList("name");

  private final Statistics statistics;
  private final String name;

  public HibernateStatisticsCollector(SessionFactory sessionFactory, String name) {
    this(sessionFactory.getStatistics(), name);
  }

  public HibernateStatisticsCollector(Statistics statistics, String name) {
    this.statistics = statistics;
    this.name = name;
  }

  @Override
  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples> metrics = new ArrayList<MetricFamilySamples>();
    metrics.addAll(getSessionMetrics());
    metrics.addAll(getConnectionMetrics());
    metrics.addAll(getCacheMetrics());
    metrics.addAll(getEntityMetrics());
    metrics.addAll(getQueryExecutionMetrics());
    return metrics;
  }

  private List<MetricFamilySamples> getSessionMetrics() {
    return Arrays.<MetricFamilySamples>asList(
        createCounter(
            "hibernate_session_opened_total",
            "Global number of sessions opened (getSessionOpenCount)",
            statistics.getSessionOpenCount()
        ),
        createCounter(
            "hibernate_session_closed_total",
            "Global number of sessions closed (getSessionCloseCount)",
            statistics.getSessionCloseCount()
        ),
        createCounter(
            "hibernate_flushes_total",
            "The global number of flush executed by sessions (getFlushCount)",
            statistics.getFlushCount()
        ),
        createCounter(
            "hibernate_connection_requested_total",
            "The global number of connections asked by the sessions (getConnectCount)",
            statistics.getConnectCount()
        ),
        createCounter(
            "hibernate_optimistic_failure_total",
            "The number of StaleObjectStateExceptions that occurred (getOptimisticFailureCount)",
            statistics.getOptimisticFailureCount()
        )
    );
  }

  private List<MetricFamilySamples> getConnectionMetrics() {
    return Arrays.<MetricFamilySamples>asList(
        createCounter(
            "hibernate_statement_prepared_total",
            "The number of prepared statements that were acquired (getPrepareStatementCount)",
            statistics.getPrepareStatementCount()
        ),
        createCounter(
            "hibernate_statement_closed_total",
            "The number of prepared statements that were released (getCloseStatementCount)",
            statistics.getCloseStatementCount()
        ),
        createCounter(
            "hibernate_transaction_total",
            "The number of transactions we know to have completed (getTransactionCount)",
            statistics.getTransactionCount()
        ),
        createCounter(
            "hibernate_transaction_success_total",
            "The number of transactions we know to have been successful (getSuccessfulTransactionCount)",
            statistics.getSuccessfulTransactionCount()
        )
    );
  }

  private List<MetricFamilySamples> getCacheMetrics() {
    return Arrays.<MetricFamilySamples>asList(
        createCounter(
            "hibernate_second_level_cache_hit_total",
            "Global number of cacheable entities/collections successfully retrieved from the cache (getSecondLevelCacheHitCount)",
            statistics.getSecondLevelCacheHitCount()
        ),
        createCounter(
            "hibernate_second_level_cache_miss_total",
            "Global number of cacheable entities/collections not found in the cache and loaded from the database (getSecondLevelCacheMissCount)",
            statistics.getSecondLevelCacheMissCount()
        ),
        createCounter(
            "hibernate_second_level_cache_put_total",
            "Global number of cacheable entities/collections put in the cache (getSecondLevelCachePutCount)",
            statistics.getSecondLevelCachePutCount()
        ),
        createCounter(
            "hibernate_query_cache_hit_total",
            "The global number of cached queries successfully retrieved from cache (getQueryCacheHitCount)",
            statistics.getQueryCacheHitCount()
        ),
        createCounter(
            "hibernate_query_cache_miss_total",
            "The global number of cached queries not found in cache (getQueryCacheMissCount)",
            statistics.getQueryCacheMissCount()
        ),
        createCounter(
            "hibernate_query_cache_put_total",
            "The global number of cacheable queries put in cache (getQueryCachePutCount)",
            statistics.getQueryCachePutCount()
        ),
        createCounter(
            "hibernate_natural_id_cache_hit_total",
            "The global number of cached naturalId lookups successfully retrieved from cache (getNaturalIdCacheHitCount)",
            statistics.getNaturalIdCacheHitCount()
        ),
        createCounter(
            "hibernate_natural_id_cache_miss_total",
            "The global number of cached naturalId lookups not found in cache (getNaturalIdCacheMissCount)",
            statistics.getNaturalIdCacheMissCount()
        ),
        createCounter(
            "hibernate_natural_id_cache_put_total",
            "The global number of cacheable naturalId lookups put in cache (getNaturalIdCachePutCount)",
            statistics.getNaturalIdCachePutCount()
        ),
        createCounter(
            "hibernate_update_timestamps_cache_hit_total",
            "The global number of timestamps successfully retrieved from cache (getUpdateTimestampsCacheHitCount)",
            statistics.getUpdateTimestampsCacheHitCount()
        ),
        createCounter(
            "hibernate_update_timestamps_cache_miss_total",
            "The global number of tables for which no update timestamps was not found in cache (getUpdateTimestampsCacheMissCount)",
            statistics.getUpdateTimestampsCacheMissCount()
        ),
        createCounter(
            "hibernate_update_timestamps_cache_put_total",
            "The global number of timestamps put in cache (getUpdateTimestampsCachePutCount)",
            statistics.getUpdateTimestampsCachePutCount()
        )
    );
  }

  private List<MetricFamilySamples> getEntityMetrics() {
    return Arrays.<MetricFamilySamples>asList(
        createCounter(
            "hibernate_entity_delete_total",
            "Global number of entity deletes (getEntityDeleteCount)",
            statistics.getEntityDeleteCount()
        ),
        createCounter(
            "hibernate_entity_insert_total",
            "Global number of entity inserts (getEntityInsertCount)",
            statistics.getEntityInsertCount()
        ),
        createCounter(
            "hibernate_entity_load_total",
            "Global number of entity loads (getEntityLoadCount)",
            statistics.getEntityLoadCount()
        ),
        createCounter(
            "hibernate_entity_fetch_total",
            "Global number of entity fetches (getEntityFetchCount)",
            statistics.getEntityFetchCount()
        ),
        createCounter(
            "hibernate_entity_update_total",
            "Global number of entity updates (getEntityUpdateCount)",
            statistics.getEntityUpdateCount()
        ),
        createCounter(
            "hibernate_collection_load_total",
            "Global number of collections loaded (getCollectionLoadCount)",
            statistics.getCollectionLoadCount()
        ),
        createCounter(
            "hibernate_collection_fetch_total",
            "Global number of collections fetched (getCollectionFetchCount)",
            statistics.getCollectionFetchCount()
        ),
        createCounter(
            "hibernate_collection_update_total",
            "Global number of collections updated (getCollectionUpdateCount)",
            statistics.getCollectionUpdateCount()
        ),
        createCounter(
            "hibernate_collection_remove_total",
            "Global number of collections removed (getCollectionRemoveCount)",
            statistics.getCollectionRemoveCount()
        ),
        createCounter(
            "hibernate_collection_recreate_total",
            "Global number of collections recreated (getCollectionRecreateCount)",
            statistics.getCollectionRecreateCount()
        )
    );
  }

  private List<MetricFamilySamples> getQueryExecutionMetrics() {
    return Arrays.<MetricFamilySamples>asList(
        createCounter(
            "hibernate_query_execution_total",
            "Global number of executed queries (getQueryExecutionCount)",
            statistics.getQueryExecutionCount()
        ),
        createCounter(
            "hibernate_natural_id_query_execution_total",
            "The global number of naturalId queries executed against the database (getNaturalIdQueryExecutionCount)",
            statistics.getNaturalIdQueryExecutionCount()
        )
    );
  }

  private CounterMetricFamily createCounter(String metric, String help, long value) {
    return new CounterMetricFamily(metric, help, LABEL_NAMES)
        .addMetric(Collections.singletonList(name), value);
  }

}
