package io.prometheus.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Summary metric, to track the size of events.
 * <p>
 * Example of uses for Summaries include:
 * <ul>
 *  <li>Response latency</li>
 *  <li>Request size</li>
 * </ul>
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
public class Summary extends SimpleCollector<Summary.Child> {

  Summary(Builder b) {
    super(b);
  }

  public static class Builder extends SimpleCollector.Builder<Builder, Summary> {
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
    private final Child child;
    private final long start;
    private Timer(Child child) {
      this.child = child;
      start = Child.timeProvider.nanoTime();
    }
    /**
     * Observe the amount of time in seconds since {@link Child#startTimer} was called.
     * @return Measured duration in seconds since {@link Child#startTimer} was called.
     */
    public double observeDuration() {
      double elapsed = (Child.timeProvider.nanoTime() - start) / NANOSECONDS_PER_SECOND;
      child.observe(elapsed);
      return elapsed;
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
      public final double count;
      public final double sum;

      private Value(double count, double sum) {
        this.count = count;
        this.sum = sum;
      }
    }

    // Having these separate leaves us open to races,
    // however Prometheus as whole has other races
    // that mean adding atomicity here wouldn't be useful.
    // This should be reevaluated in the future.
    private final DoubleAdder count = new DoubleAdder();
    private final DoubleAdder sum = new DoubleAdder();

    static NanoTimeProvider timeProvider = new NanoTimeProvider();
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
      return new Value(count.sum(), sum.sum());
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

    return familySamplesList(Type.SUMMARY, samples);
  }
}
