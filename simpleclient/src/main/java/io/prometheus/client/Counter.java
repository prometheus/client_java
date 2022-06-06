package io.prometheus.client;

import io.prometheus.client.exemplars.CounterExemplarSampler;
import io.prometheus.client.exemplars.Exemplar;
import io.prometheus.client.exemplars.ExemplarConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Counter metric, to track counts of events or running totals.
 * <p>
 * Example of Counters include:
 * <ul>
 *  <li>Number of requests processed</li>
 *  <li>Number of items that were inserted into a queue</li>
 *  <li>Total amount of data a system has processed</li>
 * </ul>
 *
 * Counters can only go up (and be reset), if your use case can go down you should use a {@link Gauge} instead.
 * Use the <code>rate()</code> function in Prometheus to calculate the rate of increase of a Counter.
 * By convention, the names of Counters are suffixed by <code>_total</code>.
 *
 * <p>
 * An example Counter:
 * <pre>
 * {@code
 *   class YourClass {
 *     static final Counter requests = Counter.build()
 *         .name("requests_total").help("Total requests.").register();
 *     static final Counter failedRequests = Counter.build()
 *         .name("requests_failed_total").help("Total failed requests.").register();
 *
 *     void processRequest() {
 *        requests.inc();
 *        try {
 *          // Your code here.
 *        } catch (Exception e) {
 *          failedRequests.inc();
 *          throw e;
 *        }
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * You can also use labels to track different types of metric:
 * <pre>
 * {@code
 *   class YourClass {
 *     static final Counter requests = Counter.build()
 *         .name("requests_total").help("Total requests.")
 *         .labelNames("method").register();
 *
 *     void processGetRequest() {
 *        requests.labels("get").inc();
 *        // Your code here.
 *     }
 *     void processPostRequest() {
 *        requests.labels("post").inc();
 *        // Your code here.
 *     }
 *   }
 * }
 * </pre>
 * These can be aggregated and processed together much more easily in the Prometheus
 * server than individual metrics for each labelset.
 *
 * If there is a suffix of <code>_total</code> on the metric name, it will be
 * removed. When exposing the time series for counter value, a
 * <code>_total</code> suffix will be added. This is for compatibility between
 * OpenMetrics and the Prometheus text format, as OpenMetrics requires the
 * <code>_total</code> suffix.
 */
public class Counter extends SimpleCollector<Counter.Child> implements Collector.Describable {

  private final Boolean exemplarsEnabled; // null means default from ExemplarConfig applies
  private final CounterExemplarSampler exemplarSampler;

  Counter(Builder b) {
    super(b);
    this.exemplarsEnabled = b.exemplarsEnabled;
    this.exemplarSampler = b.exemplarSampler;
    initializeNoLabelsChild();
  }

  public static class Builder extends SimpleCollector.Builder<Builder, Counter> {

    private Boolean exemplarsEnabled = null;
    private CounterExemplarSampler exemplarSampler = null;

    @Override
    public Counter create() {
      // Gracefully handle pre-OpenMetrics counters.
      if (name.endsWith("_total")) {
        name = name.substring(0, name.length() - 6);
      }
      dontInitializeNoLabelsChild = true;
      return new Counter(this);
    }

    /**
     * Enable exemplars and provide a custom {@link CounterExemplarSampler}.
     */
    public Builder withExemplarSampler(CounterExemplarSampler exemplarSampler) {
      if (exemplarSampler == null) {
        throw new NullPointerException();
      }
      this.exemplarSampler = exemplarSampler;
      return withExemplars();
    }

    /**
     * Allow this counter to load exemplars from a {@link CounterExemplarSampler}.
     * <p>
     * If a specific exemplar sampler is configured for this counter that exemplar sampler is used
     * (see {@link #withExemplarSampler(CounterExemplarSampler)}).
     * Otherwise the default from {@link ExemplarConfig} is used.
     */
    public Builder withExemplars() {
      this.exemplarsEnabled = TRUE;
      return this;
    }

    /**
     * Prevent this counter from loading exemplars from a {@link CounterExemplarSampler}.
     * <p>
     * You can still provide exemplars for explicitly individual observations, e.g. using
     * {@link #incWithExemplar(double, String...)}.
     */
    public Builder withoutExemplars() {
      this.exemplarsEnabled = FALSE;
      return this;
    }
  }

  /**
   * Return a Builder to allow configuration of a new Counter. Ensures required fields are provided.
   *
   * @param name The name of the metric
   * @param help The help string of the metric
   */
  public static Builder build(String name, String help) {
    return new Builder().name(name).help(help);
  }

  /**
   * Return a Builder to allow configuration of a new Counter.
   */
  public static Builder build() {
    return new Builder();
  }

  @Override
  protected Child newChild() {
    return new Child(exemplarsEnabled, exemplarSampler);
  }

  /**
   * The value of a single Counter.
   * <p>
   * <em>Warning:</em> References to a Child become invalid after using
   * {@link SimpleCollector#remove} or {@link SimpleCollector#clear},
   */
  public static class Child {
    private final DoubleAdder value = new DoubleAdder();
    private final long created = System.currentTimeMillis();
    private final Boolean exemplarsEnabled;
    private final CounterExemplarSampler exemplarSampler;
    private final AtomicReference<Exemplar> exemplar = new AtomicReference<Exemplar>();

    public Child() {
      this(null, null);
    }

    public Child(Boolean exemplarsEnabled, CounterExemplarSampler exemplarSampler) {
      this.exemplarsEnabled = exemplarsEnabled;
      this.exemplarSampler = exemplarSampler;
    }

    /**
     * Increment the counter by 1.
     */
    public void inc() {
      inc(1);
    }

    /**
     * Same as {@link #incWithExemplar(double, String...) incWithExemplar(1, exemplarLabels)}.
     */
    public void incWithExemplar(String... exemplarLabels) {
      incWithExemplar(1, exemplarLabels);
    }

    /**
     * Same as {@link #incWithExemplar(double, Map) incWithExemplar(1, exemplarLabels)}.
     */
    public void incWithExemplar(Map<String, String> exemplarLabels) {
      incWithExemplar(1, exemplarLabels);
    }

    /**
     * Increment the counter by the given amount.
     *
     * @throws IllegalArgumentException If amt is negative.
     */
    public void inc(double amt) {
      incWithExemplar(amt, (String[]) null);
    }

    /**
     * Like {@link #inc(double)}, but additionally creates an exemplar.
     * <p>
     * This exemplar takes precedence over any exemplar returned by the {@link CounterExemplarSampler} configured
     * in {@link ExemplarConfig}.
     * <p>
     * The exemplar will have {@code amt} as the value, {@code System.currentTimeMillis()} as the timestamp,
     * and the specified labels.
     *
     * @param amt            same as in {@link #inc(double)}
     * @param exemplarLabels list of name/value pairs, as documented in {@link Exemplar#Exemplar(double, String...)}.
     *                       A commonly used name is {@code "trace_id"}.
     *                       Calling {@code incWithExemplar(amt)} means that an exemplar without labels will be created.
     *                       Calling {@code incWithExemplar(amt, (String[]) null)} is equivalent
     *                       to calling {@code inc(amt)}.
     */
    public void incWithExemplar(double amt, String... exemplarLabels) {
      Exemplar exemplar = exemplarLabels == null ? null : new Exemplar(amt, System.currentTimeMillis(), exemplarLabels);
      if (amt < 0) {
        throw new IllegalArgumentException("Amount to increment must be non-negative.");
      }
      value.add(amt);
      updateExemplar(amt, exemplar);
    }

    /**
     * Same as {@link #incWithExemplar(double, String...)}, but the exemplar labels are passed as a {@link Map}.
     */
    public void incWithExemplar(double amt, Map<String, String> exemplarLabels) {
      incWithExemplar(amt, Exemplar.mapToArray(exemplarLabels));
    }

    private void updateExemplar(double amt, Exemplar userProvidedExemplar) {
      Exemplar prev, next;
      do {
        prev = exemplar.get();
        if (userProvidedExemplar == null) {
          next = sampleNextExemplar(amt, prev);
        } else {
          next = userProvidedExemplar;
        }
        if (next == null || next == prev) {
          return;
        }
      } while (!exemplar.compareAndSet(prev, next));
    }

    private Exemplar sampleNextExemplar(double amt, Exemplar prev) {
      if (FALSE.equals(exemplarsEnabled)) {
        return null;
      }
      if (exemplarSampler != null) {
        return exemplarSampler.sample(amt, prev);
      }
      if (TRUE.equals(exemplarsEnabled) || ExemplarConfig.isExemplarsEnabled()) {
        CounterExemplarSampler exemplarSampler = ExemplarConfig.getCounterExemplarSampler();
        if (exemplarSampler != null) {
          return exemplarSampler.sample(amt, prev);
        }
      }
      return null;
    }

    /**
     * Get the value of the counter.
     */
    public double get() {
      return value.sum();
    }

    private Exemplar getExemplar() {
      return exemplar.get();
    }

    /**
     * Get the created time of the counter in milliseconds.
     */
    public long created() {
      return created;
    }
  }

  // Convenience methods.

  /**
   * Increment the counter with no labels by 1.
   */
  public void inc() {
    inc(1);
  }

  /**
   * Like {@link Child#incWithExemplar(String...)}, but for the counter without labels.
   */
  public void incWithExemplar(String... exemplarLabels) {
    incWithExemplar(1, exemplarLabels);
  }

  /**
   * Like {@link Child#incWithExemplar(Map)}, but for the counter without labels.
   */
  public void incWithExemplar(Map<String, String> exemplarLabels) {
    incWithExemplar(1, exemplarLabels);
  }

  /**
   * Increment the counter with no labels by the given amount.
   *
   * @throws IllegalArgumentException If amt is negative.
   */
  public void inc(double amt) {
    noLabelsChild.inc(amt);
  }

  /**
   * Like {@link Child#incWithExemplar(double, String...)}, but for the counter without labels.
   */
  public void incWithExemplar(double amt, String... exemplarLabels) {
    noLabelsChild.incWithExemplar(amt, exemplarLabels);
  }

  /**
   * Like {@link Child#incWithExemplar(double, Map)}, but for the counter without labels.
   */
  public void incWithExemplar(double amt, Map<String, String> exemplarLabels) {
    noLabelsChild.incWithExemplar(amt, exemplarLabels);
  }

  /**
   * Get the value of the counter.
   */
  public double get() {
    return noLabelsChild.get();
  }

  @Override
  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples.Sample> samples = new ArrayList<MetricFamilySamples.Sample>(children.size());
    for(Map.Entry<List<String>, Child> c: children.entrySet()) {
      samples.add(new MetricFamilySamples.Sample(fullname + "_total", labelNames, c.getKey(), c.getValue().get(), c.getValue().getExemplar()));
      if (!DISABLE_CREATED_SERIES) {
        samples.add(new MetricFamilySamples.Sample(fullname + "_created", labelNames, c.getKey(), c.getValue().created() / 1000.0));
      }
    }
    return familySamplesList(Type.COUNTER, samples);
  }

  @Override
  public List<MetricFamilySamples> describe() {
    return Collections.<MetricFamilySamples>singletonList(new CounterMetricFamily(fullname, help, labelNames));
  }
}
