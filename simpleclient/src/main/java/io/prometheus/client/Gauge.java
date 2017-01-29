package io.prometheus.client;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Gauge metric, to report instantaneous values.
 * <p>
 * Examples of Gauges include:
 * <ul>
 *  <li>Inprogress requests</li>
 *  <li>Number of items in a queue</li>
 *  <li>Free memory</li>
 *  <li>Total memory</li>
 *  <li>Temperature</li>
 * </ul>
 *
 * Gauges can go both up and down.
 * <p>
 * An example Gauge:
 * <pre>
 * {@code
 *   class YourClass {
 *     static final Gauge inprogressRequests = Gauge.build()
 *         .name("inprogress_requests").help("Inprogress requests.").register();
 *
 *     void processRequest() {
 *        inprogressRequest.inc();
 *        // Your code here.
 *        inprogressRequest.dec();
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
 *     static final Gauge inprogressRequests = Gauge.build()
 *         .name("inprogress_requests").help("Inprogress requests.")
 *         .labelNames("method").register();
 *
 *     void processGetRequest() {
 *        inprogressRequests.labels("get").inc();
 *        // Your code here.
 *        inprogressRequests.labels("get").dec();
 *     }
 *     void processPostRequest() {
 *        inprogressRequests.labels("post").inc();
 *        // Your code here.
 *        inprogressRequests.labels("post").dec();
 *     }
 *   }
 * }
 * </pre>
 * <p>
 * These can be aggregated and processed together much more easily in the Prometheus
 * server than individual metrics for each labelset.
 */
public class Gauge extends SimpleCollector<Gauge.Child> implements Collector.Describable {

  Gauge(Builder b) {
    super(b);
  }

  public static class Builder extends SimpleCollector.Builder<Builder, Gauge> {
    @Override
    public Gauge create() {
      return new Gauge(this);
    }
  }

  /**
   *  Return a Builder to allow configuration of a new Gauge. Ensures required fields are provided.
   *
   *  @param name The name of the metric
   *  @param help The help string of the metric
   */
  public static Builder build(String name, String help) {
    return new Builder().name(name).help(help);
  }

  /**
   *  Return a Builder to allow configuration of a new Gauge.
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
   public static class Timer implements Closeable {
     private final Child child;
     private final long start;
     private Timer(Child child) {
       this.child = child;
       start = Child.timeProvider.nanoTime();
     }
     /**
      * Set the amount of time in seconds since {@link Child#startTimer} was called.
      * @return Measured duration in seconds since {@link Child#startTimer} was called.
      */
     public double setDuration() {
       double elapsed = (Child.timeProvider.nanoTime() - start) / NANOSECONDS_PER_SECOND;
       child.set(elapsed);
       return elapsed;
     }

     /**
      * Equivalent to calling {@link #setDuration()}.
      * @throws IOException
      */
     @Override
     public void close() throws IOException {
       setDuration();
     }
   }

  /**
   * The value of a single Gauge.
   * <p>
   * <em>Warning:</em> References to a Child become invalid after using
   * {@link SimpleCollector#remove} or {@link SimpleCollector#clear},
   */
  public static class Child {
    private final DoubleAdder value = new DoubleAdder();

    static TimeProvider timeProvider = new TimeProvider();
    /**
     * Increment the gauge by 1.
     */
    public void inc() {
      inc(1);
    }
    /**
     * Increment the gauge by the given amount.
     */
    public void inc(double amt) {
      value.add(amt);
    }
    /**
     * Decrement the gauge by 1.
     */
    public void dec() {
      dec(1);
    }
    /**
     * Decrement the gauge by the given amount.
     */
    public void dec(double amt) {
      value.add(-amt);
    }
    /**
     * Set the gauge to the given value.
     */
    public void set(double val) {
      synchronized(this) {
        value.reset();
        // If get() were called here it'd see an invalid value, so use a lock.
        // inc()/dec() don't need locks, as all the possible outcomes
        // are still possible if set() were atomic so no new races are introduced.
        value.add(val);
      }
    }
    /**
     * Set the gauge to the current unixtime.
     */
    public void setToCurrentTime() {
      set(timeProvider.currentTimeMillis() / MILLISECONDS_PER_SECOND);
    }
    /**
     * Start a timer to track a duration.
     * <p>
     * Call {@link Timer#setDuration} at the end of what you want to measure the duration of.
     * <p>
     * This is primarily useful for tracking the durations of major steps of batch jobs,
     * which are then pushed to a PushGateway.
     * For tracking other durations/latencies you should usually use a {@link Summary}.
     */
    public Timer startTimer() {
      return new Timer(this);
    }

    /**
     * Executes runnable code (i.e. a Java 8 Lambda) and observes a duration of how long it took to run.
     *
     * @param timeable Code that is being timed
     * @return Measured duration in seconds for timeable to complete.
     */
    public double setToTime(Runnable timeable){
      Timer timer = startTimer();

      double elapsed;
      try {
        timeable.run();
      } finally {
        elapsed = timer.setDuration();
      }

      return elapsed;
    }

    /**
     * Get the value of the gauge.
     */
    public double get() {
      synchronized(this) {
        return value.sum();
      }
    }
  }

  // Convenience methods.
  /**
   * Increment the gauge with no labels by 1.
   */
  public void inc() {
    inc(1);
  }
  /**
   * Increment the gauge with no labels by the given amount.
   */
  public void inc(double amt) {
    noLabelsChild.inc(amt);
  }
  /**
   * Increment the gauge with no labels by 1.
   */
  public void dec() {
    dec(1);
  }
  /**
   * Decrement the gauge with no labels by the given amount.
   */
  public void dec(double amt) {
    noLabelsChild.dec(amt);
  }
  /**
   * Set the gauge with no labels to the given value.
   */
  public void set(double val) {
    noLabelsChild.set(val);
  }
  /**
   * Set the gauge with no labels to the current unixtime.
   */
  public void setToCurrentTime() {
    noLabelsChild.setToCurrentTime();
  }
  /**
   * Start a timer to track a duration, for the gauge with no labels.
   * <p>
   * This is primarily useful for tracking the durations of major steps of batch jobs,
   * which are then pushed to a PushGateway.
   * For tracking other durations/latencies you should usually use a {@link Summary}.
   * <p>
   * Call {@link Timer#setDuration} at the end of what you want to measure the duration of.
   */
  public Timer startTimer() {
    return noLabelsChild.startTimer();
  }

  /**
   * Executes runnable code (i.e. a Java 8 Lambda) and observes a duration of how long it took to run.
   *
   * @param timeable Code that is being timed
   * @return Measured duration in seconds for timeable to complete.
   */
  public double setToTime(Runnable timeable){
    return noLabelsChild.setToTime(timeable);
  }

  @Override
  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples.Sample> samples = new ArrayList<MetricFamilySamples.Sample>();
    for(Map.Entry<List<String>, Child> c: children.entrySet()) {
      samples.add(new MetricFamilySamples.Sample(fullname, labelNames, c.getKey(), c.getValue().get()));
    }
    MetricFamilySamples mfs = new MetricFamilySamples(fullname, Type.GAUGE, help, samples);

    List<MetricFamilySamples> mfsList = new ArrayList<MetricFamilySamples>();
    mfsList.add(mfs);
    return mfsList;
  }

  @Override
  public List<MetricFamilySamples> describe() {
    List<MetricFamilySamples> mfsList = new ArrayList<MetricFamilySamples>();
    mfsList.add(new GaugeMetricFamily(fullname, help, labelNames));
    return mfsList;
  }

  static class TimeProvider {
    long currentTimeMillis() {
      return System.currentTimeMillis();
    }
    long nanoTime() {
      return System.nanoTime();
    }
  }
}
