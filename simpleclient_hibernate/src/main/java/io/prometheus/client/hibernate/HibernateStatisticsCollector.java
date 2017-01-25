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
 * new HibernateStatisticsCollector(sessionFactory, "default).register();
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
            "Global number of sessions opened",
            statistics.getSessionOpenCount()
        ),

        createCounter(
            "hibernate_session_closed_total",
            "Global number of sessions closed",
            statistics.getSessionCloseCount()
        ),

        createCounter(
            "hibernate_flushes_total",
            "The global number of flush executed by sessions",
            statistics.getFlushCount()
        ),

        createCounter(
            "hibernate_connection_requested_total",
            "The global number of connections asked by the sessions",
            statistics.getConnectCount()
        ),

        createCounter(
            "hibernate_optimistic_failure_total",
            "The number of StaleObjectStateExceptions that occurred",
            statistics.getOptimisticFailureCount()
        )

    );
  }

  private List<MetricFamilySamples> getConnectionMetrics() {
    return Arrays.<MetricFamilySamples>asList(

        createCounter(
            "hibernate_statement_prepared_total",
            "The number of prepared statements that were acquired",
            statistics.getPrepareStatementCount()
        ),

        createCounter(
            "hibernate_statement_closed_total",
            "The number of prepared statements that were released",
            statistics.getCloseStatementCount()
        ),

        createCounter(
            "hibernate_transaction_total",
            "The number of transactions we know to have completed",
            statistics.getTransactionCount()
        ),

        createCounter(
            "hibernate_transaction_success_total",
            "The number of transactions we know to have been successful",
            statistics.getSuccessfulTransactionCount()
        )

    );
  }

  private List<MetricFamilySamples> getCacheMetrics() {
    return Arrays.<MetricFamilySamples>asList(

        createCounter(
            "hibernate_second_level_cache_hit_total",
            "Global number of cacheable entities/collections successfully retrieved from the cache",
            statistics.getSecondLevelCacheHitCount()
        ),
        createCounter(
            "hibernate_second_level_cache_miss_total",
            "Global number of cacheable entities/collections not found in the cache and loaded from the database.",
            statistics.getSecondLevelCacheMissCount()
        ),
        createCounter(
            "hibernate_second_level_cache_put_total",
            "Global number of cacheable entities/collections put in the cache",
            statistics.getSecondLevelCachePutCount()
        ),

        createCounter(
            "hibernate_query_cache_hit_total",
            "The global number of cached queries successfully retrieved from cache",
            statistics.getQueryCacheHitCount()
        ),
        createCounter(
            "hibernate_query_cache_miss_total",
            "The global number of cached queries not found in cache",
            statistics.getQueryCacheMissCount()
        ),
        createCounter(
            "hibernate_query_cache_put_total",
            "The global number of cacheable queries put in cache",
            statistics.getQueryCachePutCount()
        ),

        createCounter(
            "hibernate_natural_id_cache_hit_total",
            "The global number of cached naturalId lookups successfully retrieved from cache",
            statistics.getNaturalIdCacheHitCount()
        ),
        createCounter(
            "hibernate_natural_id_cache_miss_total",
            "The global number of cached naturalId lookups not found in cache",
            statistics.getNaturalIdCacheMissCount()
        ),
        createCounter(
            "hibernate_natural_id_cache_put_total",
            "The global number of cacheable naturalId lookups put in cache",
            statistics.getNaturalIdCachePutCount()
        ),

        createCounter(
            "hibernate_update_timestamps_cache_hit_total",
            "The global number of timestamps successfully retrieved from cache",
            statistics.getUpdateTimestampsCacheHitCount()
        ),
        createCounter(
            "hibernate_update_timestamps_cache_miss_total",
            "The global number of tables for which no update timestamps was not found in cache",
            statistics.getUpdateTimestampsCacheMissCount()
        ),
        createCounter(
            "hibernate_update_timestamps_cache_put_total",
            "The global number of timestamps put in cache",
            statistics.getUpdateTimestampsCachePutCount()
        )

    );
  }

  private List<MetricFamilySamples> getEntityMetrics() {
    return Arrays.<MetricFamilySamples>asList(

        createCounter(
            "hibernate_entity_delete_total",
            "Global number of entity deletes",
            statistics.getEntityDeleteCount()
        ),
        createCounter(
            "hibernate_entity_insert_total",
            "Global number of entity inserts",
            statistics.getEntityInsertCount()
        ),
        createCounter(
            "hibernate_entity_load_total",
            "Global number of entity loads",
            statistics.getEntityLoadCount()
        ),
        createCounter(
            "hibernate_entity_fetch_total",
            "Global number of entity fetches",
            statistics.getEntityFetchCount()
        ),
        createCounter(
            "hibernate_entity_update_total",
            "Global number of entity updates",
            statistics.getEntityUpdateCount()
        ),

        createCounter(
            "hibernate_collection_load_total",
            "Global number of collections loaded",
            statistics.getCollectionLoadCount()
        ),
        createCounter(
            "hibernate_collection_fetch_total",
            "Global number of collections fetched",
            statistics.getCollectionFetchCount()
        ),
        createCounter(
            "hibernate_collection_update_total",
            "Global number of collections updated",
            statistics.getCollectionUpdateCount()
        ),
        createCounter(
            "hibernate_collection_remove_total",
            "Global number of collections removed",
            statistics.getCollectionRemoveCount()
        ),
        createCounter(
            "hibernate_collection_recreate_total",
            "Global number of collections recreated",
            statistics.getCollectionRecreateCount()
        )

    );
  }

  private List<MetricFamilySamples> getQueryExecutionMetrics() {
    return Arrays.<MetricFamilySamples>asList(

        createCounter(
            "hibernate_query_execution_total",
            "Global number of executed queries",
            statistics.getQueryExecutionCount()
        ),

        createCounter(
            "hibernate_natural_id_query_execution_total",
            "The global number of naturalId queries executed against the database",
            statistics.getNaturalIdQueryExecutionCount()
        )

    );
  }

  private CounterMetricFamily createCounter(String metric, String help, long value) {
    return new CounterMetricFamily(metric, help, LABEL_NAMES)
        .addMetric(Collections.singletonList(name), value);
  }

}
