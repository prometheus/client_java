package io.prometheus.client.hibernate;

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

/**
 * Collect metrics from Hibernate statistics.
 * <p>
 * Usage example:
 * <pre>
 * new HibernateStatisticsCollector(sessionFactory).register();
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

  private final Statistics statistics;

  public HibernateStatisticsCollector(SessionFactory sessionFactory) {
    this(sessionFactory.getStatistics());
  }

  public HibernateStatisticsCollector(Statistics statistics) {
    this.statistics = statistics;
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

        new CounterMetricFamily(
            "hibernate_session_opened_total",
            "Global number of sessions opened",
            statistics.getSessionOpenCount()
        ),

        new CounterMetricFamily(
            "hibernate_session_closed_total",
            "Global number of sessions closed",
            statistics.getSessionCloseCount()
        ),

        new CounterMetricFamily(
            "hibernate_flushed_total",
            "The global number of flush executed by sessions",
            statistics.getFlushCount()
        ),

        new CounterMetricFamily(
            "hibernate_connect_total",
            "The global number of connections asked by the sessions",
            statistics.getConnectCount()
        ),

        new CounterMetricFamily(
            "hibernate_optimistic_failure_total",
            "The number of StaleObjectStateExceptions that occurred",
            statistics.getOptimisticFailureCount()
        )

    );
  }

  private List<MetricFamilySamples> getConnectionMetrics() {
    return Arrays.<MetricFamilySamples>asList(

        new CounterMetricFamily(
            "hibernate_statement_prepared_total",
            "The number of prepared statements that were acquired",
            statistics.getPrepareStatementCount()
        ),

        new CounterMetricFamily(
            "hibernate_statement_closed_total",
            "The number of prepared statements that were released",
            statistics.getCloseStatementCount()
        ),

        new CounterMetricFamily(
            "hibernate_transaction_total",
            "The number of transactions we know to have completed",
            statistics.getTransactionCount()
        ),

        new CounterMetricFamily(
            "hibernate_transaction_success_total",
            "The number of transactions we know to have been successful",
            statistics.getSuccessfulTransactionCount()
        )

    );
  }

  private List<MetricFamilySamples> getCacheMetrics() {
    return Arrays.<MetricFamilySamples>asList(

        new CounterMetricFamily(
            "hibernate_second_level_cache_hit_total",
            "Global number of cacheable entities/collections successfully retrieved from the cache",
            statistics.getSecondLevelCacheHitCount()
        ),
        new CounterMetricFamily(
            "hibernate_second_level_cache_miss_total",
            "Global number of cacheable entities/collections not found in the cache and loaded from the database.",
            statistics.getSecondLevelCacheMissCount()
        ),
        new CounterMetricFamily(
            "hibernate_second_level_cache_put_total",
            "Global number of cacheable entities/collections put in the cache",
            statistics.getSecondLevelCachePutCount()
        ),

        new CounterMetricFamily(
            "hibernate_query_cache_hit_total",
            "The global number of cached queries successfully retrieved from cache",
            statistics.getQueryCacheHitCount()
        ),
        new CounterMetricFamily(
            "hibernate_query_cache_miss_total",
            "The global number of cached queries not found in cache",
            statistics.getQueryCacheMissCount()
        ),
        new CounterMetricFamily(
            "hibernate_query_cache_put_total",
            "The global number of cacheable queries put in cache",
            statistics.getQueryCachePutCount()
        ),

        new CounterMetricFamily(
            "hibernate_natural_id_cache_hit_total",
            "The global number of cached naturalId lookups successfully retrieved from cache",
            statistics.getNaturalIdCacheHitCount()
        ),
        new CounterMetricFamily(
            "hibernate_natural_id_cache_miss_total",
            "The global number of cached naturalId lookups not found in cache",
            statistics.getNaturalIdCacheMissCount()
        ),
        new CounterMetricFamily(
            "hibernate_natural_id_cache_put_total",
            "The global number of cacheable naturalId lookups put in cache",
            statistics.getNaturalIdCachePutCount()
        ),

        new CounterMetricFamily(
            "hibernate_update_timestamps_cache_hit_total",
            "The global number of timestamps successfully retrieved from cache",
            statistics.getUpdateTimestampsCacheHitCount()
        ),
        new CounterMetricFamily(
            "hibernate_update_timestamps_cache_miss_total",
            "The global number of tables for which no update timestamps was not found in cache",
            statistics.getUpdateTimestampsCacheMissCount()
        ),
        new CounterMetricFamily(
            "hibernate_update_timestamps_cache_put_total",
            "The global number of timestamps put in cache",
            statistics.getUpdateTimestampsCachePutCount()
        )

    );
  }

  private List<MetricFamilySamples> getEntityMetrics() {
    return Arrays.<MetricFamilySamples>asList(

        new CounterMetricFamily(
            "hibernate_entity_delete_total",
            "Global number of entity deletes",
            statistics.getEntityDeleteCount()
        ),
        new CounterMetricFamily(
            "hibernate_entity_insert_total",
            "Global number of entity inserts",
            statistics.getEntityInsertCount()
        ),
        new CounterMetricFamily(
            "hibernate_entity_load_total",
            "Global number of entity loads",
            statistics.getEntityLoadCount()
        ),
        new CounterMetricFamily(
            "hibernate_entity_fetch_total",
            "Global number of entity fetches",
            statistics.getEntityFetchCount()
        ),
        new CounterMetricFamily(
            "hibernate_entity_update_total",
            "Global number of entity updates",
            statistics.getEntityUpdateCount()
        ),

        new CounterMetricFamily(
            "hibernate_collection_load_total",
            "Global number of collections loaded",
            statistics.getCollectionLoadCount()
        ),
        new CounterMetricFamily(
            "hibernate_collection_fetch_total",
            "Global number of collections fetched",
            statistics.getCollectionFetchCount()
        ),
        new CounterMetricFamily(
            "hibernate_collection_update_total",
            "Global number of collections updated",
            statistics.getCollectionUpdateCount()
        ),
        new CounterMetricFamily(
            "hibernate_collection_remove_total",
            "Global number of collections removed",
            statistics.getCollectionRemoveCount()
        ),
        new CounterMetricFamily(
            "hibernate_collection_recreate_total",
            "Global number of collections recreated",
            statistics.getCollectionRecreateCount()
        )

    );
  }

  private List<MetricFamilySamples> getQueryExecutionMetrics() {
    return Arrays.<MetricFamilySamples>asList(

        new CounterMetricFamily(
            "hibernate_query_execution_total",
            "Global number of executed queries",
            statistics.getQueryExecutionCount()
        ),

        new CounterMetricFamily(
            "hibernate_natural_id_query_execution_total",
            "The global number of naturalId queries executed against the database",
            statistics.getNaturalIdQueryExecutionCount()
        )

    );
  }

}
