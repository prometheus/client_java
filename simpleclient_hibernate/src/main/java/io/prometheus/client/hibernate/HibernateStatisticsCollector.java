package io.prometheus.client.hibernate;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

/**
 * Collect metrics from one or more Hibernate SessionFactory instances.
 * <p>
 * Usage example for a single session factory:
 * <pre>
 * new HibernateStatisticsCollector(sessionFactory, "myapp").register();
 * </pre>
 * Usage example for multiple session factories:
 * <pre>
 * new HibernateStatisticsCollector()
 *     .add(sessionFactory1, "myapp1")
 *     .add(sessionFactory2, "myapp2")
 *     .register();
 * </pre>
 * If you are in a JPA environment, you can obtain the SessionFactory like this:
 * <pre>
 * SessionFactory sessionFactory =
 *     entityManagerFactory.unwrap(SessionFactory.class);
 * </pre>
 * <p>
 * When {@code enablePerQueryMetrics()} has been called, certain metrics like execution
 * time are collected per query. This may create a lot of monitoring data, so it should
 * be used with caution.
 *
 * @author Christian Kaltepoth
 */
public class HibernateStatisticsCollector extends Collector {

  private static final List<String> LABEL_NAMES = Collections.singletonList("unit");

  private static final List<String> LABEL_NAMES_PER_QUERY = Arrays.asList("unit", "query");

  private final Map<String, SessionFactory> sessionFactories = new ConcurrentHashMap<String, SessionFactory>();

  private boolean perQueryMetricsEnabled;

  /**
   * Creates an empty collector. If you use this constructor, you have to add one or more
   * session factories to the collector by calling the {@link #add(SessionFactory, String)}
   * method.
   */
  public HibernateStatisticsCollector() {
    // nothing
  }

  /**
   * Creates a new collector for the given session factory. Calling this constructor
   * has the same effect as creating an empty collector and adding the session factory
   * using {@link #add(SessionFactory, String)}.
   *
   * @param sessionFactory The Hibernate SessionFactory to collect metrics for
   * @param name A unique name for this SessionFactory
   */
  public HibernateStatisticsCollector(SessionFactory sessionFactory, String name) {
    add(sessionFactory, name);
  }

  /**
   * Registers a Hibernate SessionFactory with this collector.
   *
   * @param sessionFactory The Hibernate SessionFactory to collect metrics for
   * @param name A unique name for this SessionFactory
   * @return Returns the collector
   */
  public HibernateStatisticsCollector add(SessionFactory sessionFactory, String name) {
    sessionFactories.put(name, sessionFactory);
    return this;
  }

  /**
   * Enables collection of per-query metrics. Produces a lot of monitoring data, so use with caution.
   * <p>
   * Per-query metrics have a label "query" with the actual HQL query as value. The query will contain
   * placeholders ("?") instead of the real parameter values (example: {@code select u from User u where id=?}).
   *
   * @return Returns the collector
   */
  public HibernateStatisticsCollector enablePerQueryMetrics() {
    this.perQueryMetricsEnabled = true;
    return this;
  }

  @Override
  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples> metrics = new ArrayList<MetricFamilySamples>();
    metrics.addAll(getSessionMetrics());
    metrics.addAll(getConnectionMetrics());
    metrics.addAll(getCacheMetrics());
    metrics.addAll(getEntityMetrics());
    metrics.addAll(getQueryExecutionMetrics());
    if (perQueryMetricsEnabled) {
      metrics.addAll(getPerQueryMetrics());
    }
    return metrics;
  }

  @Override
  public <T extends Collector> T register(CollectorRegistry registry) {
    if (sessionFactories.isEmpty()) {
      throw new IllegalStateException("You must register at least one SessionFactory.");
    }
    return super.register(registry);
  }

  private List<MetricFamilySamples> getSessionMetrics() {
    return Arrays.<MetricFamilySamples>asList(
        createCounter(
            "hibernate_session_opened_total",
            "Global number of sessions opened (getSessionOpenCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getSessionOpenCount();
              }
            }
        ),
        createCounter(
            "hibernate_session_closed_total",
            "Global number of sessions closed (getSessionCloseCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getSessionCloseCount();
              }
            }
        ),
        createCounter(
            "hibernate_flushed_total",
            "The global number of flushes executed by sessions (getFlushCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getFlushCount();
              }
            }
        ),
        createCounter(
            "hibernate_connect_total",
            "The global number of connections requested by the sessions (getConnectCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getConnectCount();
              }
            }
        ),
        createCounter(
            "hibernate_optimistic_failure_total",
            "The number of StaleObjectStateExceptions that occurred (getOptimisticFailureCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getOptimisticFailureCount();
              }
            }
        )
    );
  }

  private List<MetricFamilySamples> getConnectionMetrics() {
    return Arrays.<MetricFamilySamples>asList(
        createCounter(
            "hibernate_statement_prepared_total",
            "The number of prepared statements that were acquired (getPrepareStatementCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getPrepareStatementCount();
              }
            }
        ),
        createCounter(
            "hibernate_statement_closed_total",
            "The number of prepared statements that were released (getCloseStatementCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getCloseStatementCount();
              }
            }
        ),
        createCounter(
            "hibernate_transaction_total",
            "The number of transactions we know to have completed (getTransactionCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getTransactionCount();
              }
            }
        ),
        createCounter(
            "hibernate_transaction_success_total",
            "The number of transactions we know to have been successful (getSuccessfulTransactionCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getSuccessfulTransactionCount();
              }
            }
        )
    );
  }

  private List<MetricFamilySamples> getCacheMetrics() {
    return Arrays.<MetricFamilySamples>asList(
        createCounter(
            "hibernate_second_level_cache_hit_total",
            "Global number of cacheable entities/collections successfully retrieved from the cache (getSecondLevelCacheHitCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getSecondLevelCacheHitCount();
              }
            }
        ),
        createCounter(
            "hibernate_second_level_cache_miss_total",
            "Global number of cacheable entities/collections not found in the cache and loaded from the database (getSecondLevelCacheMissCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getSecondLevelCacheMissCount();
              }
            }
        ),
        createCounter(
            "hibernate_second_level_cache_put_total",
            "Global number of cacheable entities/collections put in the cache (getSecondLevelCachePutCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getSecondLevelCachePutCount();
              }
            }
        ),
        createCounter(
            "hibernate_query_cache_hit_total",
            "The global number of cached queries successfully retrieved from cache (getQueryCacheHitCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getQueryCacheHitCount();
              }
            }
        ),
        createCounter(
            "hibernate_query_cache_miss_total",
            "The global number of cached queries not found in cache (getQueryCacheMissCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getQueryCacheMissCount();
              }
            }
        ),
        createCounter(
            "hibernate_query_cache_put_total",
            "The global number of cacheable queries put in cache (getQueryCachePutCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getQueryCachePutCount();
              }
            }
        ),
        createCounter(
            "hibernate_natural_id_cache_hit_total",
            "The global number of cached naturalId lookups successfully retrieved from cache (getNaturalIdCacheHitCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getNaturalIdCacheHitCount();
              }
            }
        ),
        createCounter(
            "hibernate_natural_id_cache_miss_total",
            "The global number of cached naturalId lookups not found in cache (getNaturalIdCacheMissCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getNaturalIdCacheMissCount();
              }
            }
        ),
        createCounter(
            "hibernate_natural_id_cache_put_total",
            "The global number of cacheable naturalId lookups put in cache (getNaturalIdCachePutCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getNaturalIdCachePutCount();
              }
            }
        ),
        createCounter(
            "hibernate_update_timestamps_cache_hit_total",
            "The global number of timestamps successfully retrieved from cache (getUpdateTimestampsCacheHitCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getUpdateTimestampsCacheHitCount();
              }
            }
        ),
        createCounter(
            "hibernate_update_timestamps_cache_miss_total",
            "The global number of tables for which no update timestamps was not found in cache (getUpdateTimestampsCacheMissCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getUpdateTimestampsCacheMissCount();
              }
            }
        ),
        createCounter(
            "hibernate_update_timestamps_cache_put_total",
            "The global number of timestamps put in cache (getUpdateTimestampsCachePutCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getUpdateTimestampsCachePutCount();
              }
            }
        )
    );
  }

  private List<MetricFamilySamples> getEntityMetrics() {
    return Arrays.<MetricFamilySamples>asList(
        createCounter(
            "hibernate_entity_delete_total",
            "Global number of entity deletes (getEntityDeleteCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getEntityDeleteCount();
              }
            }
        ),
        createCounter(
            "hibernate_entity_insert_total",
            "Global number of entity inserts (getEntityInsertCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getEntityInsertCount();
              }
            }
        ),
        createCounter(
            "hibernate_entity_load_total",
            "Global number of entity loads (getEntityLoadCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getEntityLoadCount();
              }
            }
        ),
        createCounter(
            "hibernate_entity_fetch_total",
            "Global number of entity fetches (getEntityFetchCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getEntityFetchCount();
              }
            }
        ),
        createCounter(
            "hibernate_entity_update_total",
            "Global number of entity updates (getEntityUpdateCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getEntityUpdateCount();
              }
            }
        ),
        createCounter(
            "hibernate_collection_load_total",
            "Global number of collections loaded (getCollectionLoadCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getCollectionLoadCount();
              }
            }
        ),
        createCounter(
            "hibernate_collection_fetch_total",
            "Global number of collections fetched (getCollectionFetchCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getCollectionFetchCount();
              }
            }
        ),
        createCounter(
            "hibernate_collection_update_total",
            "Global number of collections updated (getCollectionUpdateCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getCollectionUpdateCount();
              }
            }
        ),
        createCounter(
            "hibernate_collection_remove_total",
            "Global number of collections removed (getCollectionRemoveCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getCollectionRemoveCount();
              }
            }
        ),
        createCounter(
            "hibernate_collection_recreate_total",
            "Global number of collections recreated (getCollectionRecreateCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getCollectionRecreateCount();
              }
            }
        )
    );
  }

  private List<MetricFamilySamples> getQueryExecutionMetrics() {
    return Arrays.<MetricFamilySamples>asList(
        createCounter(
            "hibernate_query_execution_total",
            "Global number of executed queries (getQueryExecutionCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getQueryExecutionCount();
              }
            }
        ),
        createCounter(
            "hibernate_natural_id_query_execution_total",
            "The global number of naturalId queries executed against the database (getNaturalIdQueryExecutionCount)",
            new ValueProvider() {
              @Override
              public double getValue(Statistics statistics) {
                return statistics.getNaturalIdQueryExecutionCount();
              }
            }
        )
    );
  }

  private List<MetricFamilySamples> getPerQueryMetrics() {
    List<MetricFamilySamples> metrics = new ArrayList<MetricFamilySamples>();

    metrics.addAll(Arrays.asList(

        createCounterForQuery("hibernate_per_query_cache_hit_total",
                "Global number of cache hits for query (getCacheHitCount)",
            new ValueProviderPerQuery() {
              @Override
              public double getValue(Statistics statistics, String query) {
                return statistics.getQueryStatistics(query)
                        .getCacheHitCount();
              }
            }
        ),
        createCounterForQuery("hibernate_per_query_cache_miss_total",
                "Global number of cache misses for query (getCacheMissCount)",
            new ValueProviderPerQuery() {
              @Override
              public double getValue(Statistics statistics, String query) {
                return statistics.getQueryStatistics(query)
                        .getCacheMissCount();
              }
            }
        ),
        createCounterForQuery("hibernate_per_query_cache_put_total",
                "Global number of cache puts for query (getCachePutCount)",
            new ValueProviderPerQuery() {
              @Override
              public double getValue(Statistics statistics, String query) {
                return statistics.getQueryStatistics(query)
                        .getCachePutCount();
              }
            }
        ),
        createCounterForQuery("hibernate_per_query_execution_total",
                "Global number of executions for query (getExecutionCount)",
            new ValueProviderPerQuery() {
              @Override
              public double getValue(Statistics statistics, String query) {
                return statistics.getQueryStatistics(query)
                        .getExecutionCount();
              }
            }
        ),
        createCounterForQuery("hibernate_per_query_execution_rows_total",
                "Global number of rows for all executions of query (getExecutionRowCount)",
            new ValueProviderPerQuery() {
              @Override
              public double getValue(Statistics statistics, String query) {
                return statistics.getQueryStatistics(query)
                        .getExecutionRowCount();
              }
            }
        ),
        createGaugeForQuery("hibernate_per_query_execution_min_seconds",
                "Minimum execution time of query in seconds (based on getExecutionMinTime)",
            new ValueProviderPerQuery() {
              @Override
              public double getValue(Statistics statistics, String query) {
                return toSeconds(statistics.getQueryStatistics(query)
                        .getExecutionMinTime());
              }
            }
        ),
        createGaugeForQuery("hibernate_per_query_execution_max_seconds",
                "Maximum execution time of query in seconds (based on getExecutionMaxTime)",
            new ValueProviderPerQuery() {
              @Override
              public double getValue(Statistics statistics, String query) {
                return toSeconds(statistics.getQueryStatistics(query)
                        .getExecutionMaxTime());
              }
            }
        ),
        createCounterForQuery("hibernate_per_query_execution_seconds_total",
            "Accumulated execution time of query in seconds (based on getExecutionTotalTime)",
            new ValueProviderPerQuery() {
              @Override
              public double getValue(Statistics statistics, String query) {
                return toSeconds(statistics.getQueryStatistics(query)
                    .getExecutionTotalTime());
              }
            }
        )
    ));

    return metrics;
  }

  private CounterMetricFamily createCounter(String metric, String help, ValueProvider provider) {

    CounterMetricFamily metricFamily = new CounterMetricFamily(metric, help, LABEL_NAMES);

    for (Entry<String, SessionFactory> entry : sessionFactories.entrySet()) {
      metricFamily.addMetric(
              Collections.singletonList(entry.getKey()),
              provider.getValue(entry.getValue().getStatistics())
      );
    }

    return metricFamily;

  }

  private CounterMetricFamily createCounterForQuery(String metric, String help, ValueProviderPerQuery provider) {

    final CounterMetricFamily counters = new CounterMetricFamily(metric, help, LABEL_NAMES_PER_QUERY);

    addMetricsForQuery(new PerQuerySamples() {
      @Override
      public void addMetric(List<String> labelValues, double value) {
        counters.addMetric(labelValues, value);
      }
    }, provider);

    return counters;

  }

  private GaugeMetricFamily createGaugeForQuery(String metric, String help, ValueProviderPerQuery provider) {

    final GaugeMetricFamily gauges = new GaugeMetricFamily(metric, help, LABEL_NAMES_PER_QUERY);

    addMetricsForQuery(new PerQuerySamples() {
      @Override
      public void addMetric(List<String> labelValues, double value) {
        gauges.addMetric(labelValues, value);
      }
    }, provider);

    return gauges;

  }

  private void addMetricsForQuery(PerQuerySamples samples, ValueProviderPerQuery provider) {

    for (Entry<String, SessionFactory> entry : sessionFactories.entrySet()) {
      SessionFactory sessionFactory = entry.getValue();
      Statistics stats = sessionFactory.getStatistics();
      String unitName = entry.getKey();

      for (String query : stats.getQueries()) {
        samples.addMetric(Arrays.asList(unitName, query), provider.getValue(stats, query));
      }
    }
  }

  private double toSeconds(long milliseconds){
    return milliseconds / 1000d;
  }

  private interface PerQuerySamples {

    void addMetric(List<String> labelValues, double value);

  }


  private interface ValueProvider {

    double getValue(Statistics statistics);

  }

  private interface ValueProviderPerQuery {

    double getValue(Statistics statistics, String query);

  }


}
