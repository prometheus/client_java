package io.prometheus.client;

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
 * These can be aggregated and processed together much more easily in the Promtheus 
 * server than individual metrics for each labelset.
 */
public class Gauge extends SimpleCollector<Gauge.Child, Gauge> {
  
  Gauge(Builder b) {
    super(b);
  }

  public static class Builder extends SimpleCollector.Builder<Gauge> {
    @Override
    public Gauge create() {
      return new Gauge(this);
    }
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
   * The value of a single Gauge.
   * <p>
   * <em>Warning:</em> References to a Child become invalid after using
   * {@link SimpleCollector#remove} or {@link SimpleCollector#clear},
   */
  public static class Child {
    private volatile double value;

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
      synchronized(this){
        value += amt;
      }
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
      synchronized(this){
        value -= amt;
      }
    }
    /**
     * Set the gauge to the given value.
     */
    public void set(double val) {
      synchronized(this){
        value = val;
      }
    }
    /**
     * Set the gauge to the current unixtime.
     */
    public void setToCurrentTime() {
      set(timeProvider.currentTimeMillis() / MILLISECONDS_PER_SECOND);
    }
    /**
     * Get the value of the gauge.
     */
    public double get() {
      return value;
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
    labels().inc(amt);
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
    labels().dec(amt);
  }
  /**
   * Set the gauge with no labels to the given value.
   */
  public void set(double val) {
    labels().set(val);
  }
  /**
   * Set the gauge with no labels to the current unixtime.
   */
  public void setToCurrentTime() {
    labels().setToCurrentTime();
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

  static class TimeProvider {
    long currentTimeMillis() {
      return System.currentTimeMillis();
    }
  }
}
