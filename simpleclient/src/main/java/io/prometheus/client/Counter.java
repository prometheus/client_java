package io.prometheus.client;

import java.util.List;
import java.util.Map;
import java.util.Vector;

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
 *     static final Counter requests = (Counter) Counter.build()
 *         .name("requests_total").help("Total requests.").register();
 *     static final Counter failedRequests = (Counter) Counter.build()
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
 *     static final Counter requests = (Counter) Counter.build()
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
 * These can be aggregated and processed together much more easily in the Promtheus 
 * server than individual metrics for each labelset.
 */
public class Counter extends SimpleCollector<Counter.Child> {

  public Counter(Builder b) {
    super(b);
    if (labelNames.length == 0) {
      inc(0);
    }
  }

  public static class Builder extends SimpleCollector.Builder {
    @Override
    public SimpleCollector create() {
      return new Counter(this);
    }
  }

  /**
   *  Return a Builder to allow configuration of a new Counter.
   */
  public static Builder build() {
    return new Builder();
  }

  @Override
  protected Child newChild() {
    return new Child();
  }

  /**
   * The value of a single Counter.
   * <p>
   * <em>Warning:</em> References to a Child become invalid after using
   * {@link SimpleCollector#remove} or {@link SimpleCollector#clear},
   */
  public static class Child {
    private volatile double value;
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
      synchronized(this){
        value += amt;
      }
    }
    /**
     * Get the value of the counter.
     */
    public double get() {
      return value;
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
    labels().inc(amt);
  }

  @Override
  public MetricFamilySamples[] collect() {
    Vector<MetricFamilySamples.Sample> samples = new Vector<MetricFamilySamples.Sample>();
    for(Map.Entry<List<String>, Child> c: children.entrySet()) {
      samples.add(new MetricFamilySamples.Sample(fullname, labelNames, c.getKey(), c.getValue().get()));
    }

    MetricFamilySamples mfs = new MetricFamilySamples(fullname, Type.COUNTER, help, samples);
    return new MetricFamilySamples[]{mfs};
  }
}
