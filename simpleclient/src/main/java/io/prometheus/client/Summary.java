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
 *         .name("requests_size_bytes").help("Request size in bytes.").register();
 *     static final Summary requestLatency = Summary.build()
 *         .name("requests_latency_seconds").help("Request latency in seconds.").register();
 *
 *     void processRequest(Request req) {  
 *        Summary.Timer requestTimer = requestLatency.startTimer();
 *        try {
 *          // Your code here.
 *        } finally {
 *          receivedBytes.observe(req.size());
 *          requestTimer.observeDuration();
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
   * Represents an event being timed.
   */
  public static class Timer {
    Child child;
    long start;
    private Timer(Child child) {
      this.child = child;
      start = Child.timeProvider.nanoTime();
    }
    /**
     * Observe the amount of time in seconds since {@link Child#startTimer} was called.
     */
    public void observeDuration() {
      child.observe((Child.timeProvider.nanoTime() - start) / NANOSECONDS_PER_SECOND);
    }
  }

  /**
   * The value of a single Summary.
   * <p>
   * <em>Warning:</em> References to a Child become invalid after using
   * {@link SimpleCollector#remove} or {@link SimpleCollector#clear}.
   */
  public static class Child {
    public static class Value {
      private double count;  
      private double sum;
    }

    // Having these seperate leaves us open to races,
    // however Prometheus as whole has other races
    // that mean adding atomicity here wouldn't be useful.
    // This should be reevaluated in the future.
    private DoubleAdder count = new DoubleAdder();  
    private DoubleAdder sum = new DoubleAdder();

    static TimeProvider timeProvider = new TimeProvider();
    /**
     * Observe the given amount.
     */
    public void observe(double amt) {
      count.add(1);
      sum.add(amt);
    }
    /**
     * Start a timer to track a duration.
     * <p>
     * Call {@link Timer#observeDuration} at the end of what you want to measure the duration of.
     */
    public Timer startTimer() {
      return new Timer(this);
    }
    /**
     * Get the value of the Summary.
     * <p>
     * <em>Warning:</em> The definition of {@link Value} is subject to change.
     */
    public Value get() {
      Value v = new Value();
      v.count = count.sum();
      v.sum = sum.sum();
      return v;
    }
  }

  // Convenience methods.
  /**
   * Observe the given amount on the summary with no labels.
   */
  public void observe(double amt) {
    noLabelsChild.observe(amt);
  }
  /**
   * Start a timer to track a duration on the summary with no labels.
   * <p>
   * Call {@link Timer#observeDuration} at the end of what you want to measure the duration of.
   */
  public Timer startTimer() {
    return noLabelsChild.startTimer();
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
