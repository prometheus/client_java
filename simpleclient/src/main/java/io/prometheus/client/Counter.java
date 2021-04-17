package io.prometheus.client;

import io.prometheus.client.exemplars.api.CounterExemplarSampler;
import io.prometheus.client.exemplars.api.Exemplar;
import io.prometheus.client.exemplars.api.ExemplarConfig;
import io.prometheus.client.exemplars.api.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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

  private final CounterExemplarSampler exemplarSampler;

  Counter(Builder b) {
    super(b);
    this.exemplarSampler = b.exemplarSampler;
    initializeNoLabelsChild();
  }

  public static class Builder extends SimpleCollector.Builder<Builder, Counter> {

    private CounterExemplarSampler exemplarSampler = ExemplarConfig.getCounterExemplarSampler();

    @Override
    public Counter create() {
      // Gracefully handle pre-OpenMetrics counters.
      if (name.endsWith("_total")) {
        name = name.substring(0, name.length() - 6);
      }
      dontInitializeNoLabelsChild = true;
      return new Counter(this);
    }

    public Builder withExemplars(CounterExemplarSampler exemplarSampler) {
      if (exemplarSampler == null) {
        throw new NullPointerException();
      }
      this.exemplarSampler = exemplarSampler;
      return this;
    }

    public Builder withExemplars() {
      return withExemplars(ExemplarConfig.getDefaultExemplarSampler());
    }

    public Builder withoutExemplars() {
      return withExemplars(ExemplarConfig.getNoopExemplarSampler());
    }
  }


  /**
   *  Return a Builder to allow configuration of a new Counter. Ensures required fields are provided.
   *
   *  @param name The name of the metric
   *  @param help The help string of the metric
   */
  public static Builder build(String name, String help) {
    return new Builder().name(name).help(help);
  }

  /**
   *  Return a Builder to allow configuration of a new Counter.
   */
  public static Builder build() {
    return new Builder();
  }

  @Override
  protected Child newChild() {
    return new Child(exemplarSampler);
  }

  /**
   * The value of a single Counter.
   * <p>
   * <em>Warning:</em> References to a Child become invalid after using
   * {@link SimpleCollector#remove} or {@link SimpleCollector#clear},
   */
  public static class Child implements Value {
    private final DoubleAdder value = new DoubleAdder();
    private final long created = System.currentTimeMillis();
    private final CounterExemplarSampler exemplarSampler;
    private final AtomicReference<Exemplar> exemplar = new AtomicReference<Exemplar>();

    public Child(CounterExemplarSampler exemplarSampler) {
      this.exemplarSampler = exemplarSampler;
    }

    /**
     * Increment the counter by 1.
     */
    public void inc() {
      inc(1);
    }
    /**
     * Increment the counter by the given amount.
     * @throws IllegalArgumentException If amt is negative.
     */
    public void inc(double amt) {
      if (amt < 0) {
        throw new IllegalArgumentException("Amount to increment must be non-negative.");
      }
      value.add(amt);
      updateExemplar(amt);
    }
    private void updateExemplar(double amt) {
      Exemplar prev, next;
      do {
        prev = exemplar.get();
        next = exemplarSampler.sample(amt, this, prev);
        if (next == null || next == prev) {
          return;
        }
      } while (!exemplar.compareAndSet(prev, next));
    }
    /**
     * Get the value of the counter.
     */
    @Override
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
   * Increment the counter with no labels by the given amount.
   * @throws IllegalArgumentException If amt is negative.
   */
  public void inc(double amt) {
    noLabelsChild.inc(amt);
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
      samples.add(new MetricFamilySamples.Sample(fullname + "_created", labelNames, c.getKey(), c.getValue().created() / 1000.0));
    }
    return familySamplesList(Type.COUNTER, samples);
  }

  @Override
  public List<MetricFamilySamples> describe() {
    return Collections.<MetricFamilySamples>singletonList(new CounterMetricFamily(fullname, help, labelNames));
  }
}
