package io.prometheus.client;

import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Summary metric, to track distributions and frequencies..
 * <p>
 * Example of uses for Summaries include:
 * <ul>
 *  <li>Response latency</li>
 *  <li>Request size</li>
 * </ul>
 * <p>
 * <em>Note:</em> Full distribution support is still a work in progress. Currently it reports only
 * the count of observations and their sum, allowing for calculation of the rate of 
 * observations and the average observation size.
 * 
 * <p>
 * An example Summary:
 * <pre>
 * {@code
 *   class YourClass {
 *     static final Summary requestLatency = (Summary) Summary.build()
 *         .name("requests_latency_s_total").help("Request latency in seconds.").register();
 *
 *     void processRequest() {  
 *        long start = System.nanoTime();
 *        try {
 *          // Your code here.
 *        } finally {
 *          requestLatency.observe((System.nanoTime() - start) / 1000000000.0);
 *        }
 *     }
 *   }
 * }
 * </pre>
 */
public class Summary extends SimpleCollector<Summary.Child> {

  public Summary(Builder b) {
    super(b);
    if (labelNames.length == 0) {
      labels().set(0, 0);
    }
  }

  public static class Builder extends SimpleCollector.Builder {
    @Override
    public SimpleCollector create() {
      return new Summary(this);
    }
  }

  /**
   *  Return a Builder to allow configuration of a new Summary.
   */
  public static Builder build() {
    return new Builder();
  }

  @Override
  protected Child newChild() {
    return new Child();
  }

  /**
   * The value of a single Summary.
   * <p>
   * <em>Warning:</em> References to a Child become invalid after using
   * {@link SimpleCollector#remove} or {@link SimpleCollector#clear},
   */
  public static class Child {
    public static class Value {
      private volatile double count;
      private volatile double sum;
    }
    private final Value value = new Value();
    /**
     * Observe the given amount.
     */
    public void observe(double amt) {
      synchronized(this){
        value.count++;
        value.sum += amt;
      }
    }
    void set(double c, double s) {
      synchronized(this){
        value.count = c;
        value.sum = s;
      }
    }
    /**
     * Get the value of the Summary.
     * <p>
     * <em>Warning:</em> The definition of {@link Value} is subject to change.
     */
    public Value get() {
      Value v = new Value();
      synchronized(this){
        v.sum = value.sum;
        v.count = value.count;
      }
      return v;
    }
  }

  // Convenience method.
  /**
   * Observe the given amount on the Summary with no labels.
   */
  public void observe(double amt) {
    labels().observe(amt);
  }

  @Override
  public MetricFamilySamples[] collect() {
    Vector<MetricFamilySamples.Sample> samples = new Vector<MetricFamilySamples.Sample>();
    for(Map.Entry<List<String>, Child> c: children.entrySet()) {
      Child.Value v = c.getValue().get();
      samples.add(new MetricFamilySamples.Sample(fullname + "_count", labelNames, c.getKey(), v.count));
      samples.add(new MetricFamilySamples.Sample(fullname + "_sum", labelNames, c.getKey(), v.sum));
    }

    MetricFamilySamples mfs = new MetricFamilySamples(fullname, Type.SUMMARY, help, samples);
    return new MetricFamilySamples[]{mfs};
  }
}
