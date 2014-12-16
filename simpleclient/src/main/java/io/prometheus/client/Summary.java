package io.prometheus.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
 * Example Summaries:
 * <pre>
 * {@code
 *   class YourClass {
 *     static final Summary receivedBytes = Summary.build()
 *         .name("requests_size_bytes_total").help("Request size in bytes.").register();
 *     static final Summary requestLatency = Summary.build()
 *         .name("requests_latency_s_total").help("Request latency in seconds.").register();
 *
 *     void processRequest(Request req) {  
 *        long start = System.nanoTime();
 *        try {
 *          // Your code here.
 *        } finally {
 *          requestLatency.observe(req.size());
 *          requestLatency.observeSecondsSinceNanoTime(start);
 *        }
 *     }
 *   }
 * }
 * </pre>
 * This would allow you to track request rate, average latency and average request size.
 */
public class Summary extends SimpleCollector<Summary.Child, Summary> {

  Summary(Builder b) {
    super(b);
  }

  public static class Builder extends SimpleCollector.Builder<Summary> {
    @Override
    public Summary create() {
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
   * {@link SimpleCollector#remove} or {@link SimpleCollector#clear}.
   */
  public static class Child {
    public static class Value {
      private volatile double count;
      private volatile double sum;
    }
    private final Value value = new Value();

    static TimeProvider timeProvider = new TimeProvider();
    /**
     * Observe the given amount.
     */
    public void observe(double amt) {
      synchronized(this){
        value.count++;
        value.sum += amt;
      }
    }
    /**
     * Observe the number of seconds since the given nanoTime.
     * <p>
     * This should be passed a previous result of {@link System.nanoTime}.
     */
    public void observeSecondsSinceNanoTime(long nanoTime) {
      observe((timeProvider.nanoTime() - nanoTime) / NANOSECONDS_PER_SECOND);
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
   * Observe the given amount on the summary with no labels.
   */
  public void observe(double amt) {
    labels().observe(amt);
  }
  /**
   * Observe the number of seconds since the given nanoTime on the summary with no labels.
   * <p>
   * This should be passed a previous result of {@link System.nanoTime}.
   */
  public void observeSecondsSinceNanoTime(long nanoTime) {
    labels().observeSecondsSinceNanoTime(nanoTime);
  }

  @Override
  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples.Sample> samples = new ArrayList<MetricFamilySamples.Sample>();
    for(Map.Entry<List<String>, Child> c: children.entrySet()) {
      Child.Value v = c.getValue().get();
      samples.add(new MetricFamilySamples.Sample(fullname + "_count", labelNames, c.getKey(), v.count));
      samples.add(new MetricFamilySamples.Sample(fullname + "_sum", labelNames, c.getKey(), v.sum));
    }

    MetricFamilySamples mfs = new MetricFamilySamples(fullname, Type.SUMMARY, help, samples);
    List<MetricFamilySamples> mfsList = new ArrayList<MetricFamilySamples>();
    mfsList.add(mfs);
    return mfsList;
  }

  static class TimeProvider {
    long nanoTime() {
      return System.nanoTime();
    }
  }
}
