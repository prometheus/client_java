package io.prometheus.client;

import java.util.*;

/**
 * Histogram metric, to track distributions of events.
 * <p>
 * Example of uses for Histograms include:
 * <ul>
 *  <li>Response latency</li>
 *  <li>Request size</li>
 * </ul>
 * <p>
 * <em>Note:</em> Each bucket is one timeseries. Many buckets and/or many dimensions with labels
 * can produce large amount of time series, that may cause performance problems.
 * 
 * <p>
 * The default buckets are intended to cover a typical web/rpc request from milliseconds to seconds.
 * <p>
 * Example Histograms:
 * <pre>
 * {@code
 *   class YourClass {
 *     static final Histogram requestLatency = Histogram.build()
 *         .name("requests_latency_seconds").help("Request latency in seconds.").register();
 *
 *     void processRequest(Request req) {  
 *        Histogram.Timer requestTimer = requestLatency.startTimer();
 *        try {
 *          // Your code here.
 *        } finally {
 *          requestTimer.observeDuration();
 *        }
 *     }
 *   }
 * }
 * </pre>
 * <p>
 * You can choose your own buckets:
 * <pre>
 * {@code
 *     static final Histogram requestLatency = Histogram.build()
 *         .buckets(.01, .02, .03, .04)
 *         .name("requests_latency_seconds").help("Request latency in seconds.").register();
 * }
 * </pre>
 * {@link Histogram.Builder#linearBuckets(double, double, int) linearBuckets} and
 * {@link Histogram.Builder#exponentialBuckets(double, double, int) exponentialBuckets}
 * offer easy ways to set common bucket patterns.
 */
public class Histogram extends SimpleCollector<Histogram.Child> {
  private final Double[] buckets;

  Histogram(Builder b) {
    super(b);
    buckets = b.getBuckets();
    initializeNoLabelsChild();
  }

  public static class Builder extends SimpleCollector.Builder<Builder, Histogram> {
    private static final Double[] DEFAULT_BUCKETS = new Double[]{.005, .01, .025, .05, .075, .1, .25, .5, .75, 1.0, 2.5, 5.0, 7.5, 10.0};
    private final List<Double> bucketsList = new LinkedList<Double>();

    @Override
    public Histogram create() {
      if (bucketsList.isEmpty()) {
          throw new IllegalStateException("Histogram must have at least one bucket.");
      }
      for (String label: labelNames) {
        if (label.equals("le")) {
            throw new IllegalStateException("Histogram cannot have a label named 'le'.");
        }
      }

      dontInitializeNoLabelsChild = true;
      return new Histogram(this);
    }

    /**
     * Return sorted and sanitized (with infinity as last item) buckets list.
     *
     * @return
       */
    public Double[] getBuckets() {
      Double[] buckets;
      if(this.bucketsList == null || this.bucketsList.isEmpty()) {
        buckets = DEFAULT_BUCKETS;
      } else {
        Collections.sort(this.bucketsList);
        buckets = this.bucketsList.toArray(new Double[this.bucketsList.size()]);
      }
      for (int i = 0; i < buckets.length - 1; i++) {
        if (buckets[i] > buckets[i + 1]) {
          throw new IllegalStateException("Histogram buckets must be in increasing order: "
              + buckets[i] + " > " + buckets[i + 1]);
        }
      }
      // Append infinity bucket if it's not already there.
      if (buckets[buckets.length - 1] != Double.POSITIVE_INFINITY) {
        Double[] tmp = new Double[buckets.length + 1];
        System.arraycopy(buckets, 0, tmp, 0, buckets.length);
        tmp[buckets.length] = Double.POSITIVE_INFINITY;
        buckets = tmp;
      }
      return buckets;
    }
   
    /**
      * Adds the upper bounds of buckets for the histogram.
      */
    public Builder buckets(Double... buckets) {
      this.bucketsList.addAll(Arrays.asList(buckets));
      return this;
    }

    /**
      * Adds the upper bounds of buckets for the histogram with a linear sequence.
      */
    public Builder linearBuckets(double start, double width, int count) {
      for (int i = 0; i < count; i++){
        bucketsList.add(start + i*width);
      }
      return this;
    }
    /**
      * Adds the upper bounds of buckets for the histogram with an exponential sequence.
      */
    public Builder exponentialBuckets(double start, double factor, int count) {
      for (int i = 0; i < count; i++) {
        bucketsList.add(start * Math.pow(factor, i));
      }
      return this;
    }
  }

  /**
   *  Return a Builder to allow configuration of a new Histogram.
   */
  public static Builder build() {
    return new Builder();
  }

  @Override
  protected Child newChild() {
    return new Child(buckets);
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
   * The value of a single Histogram.
   * <p>
   * <em>Warning:</em> References to a Child become invalid after using
   * {@link SimpleCollector#remove} or {@link SimpleCollector#clear}.
   */
  public static class Child {
    public static class Value {
      public final double sum;
      public final double[] buckets;

      public Value(double sum, double[] buckets) {
        this.sum = sum;
        this.buckets = buckets;
      }
    }

    private Child(Double[] buckets) {
      upperBounds = buckets;
      cumulativeCounts = new DoubleAdder[buckets.length];
      for (int i = 0; i < buckets.length; ++i) {
        cumulativeCounts[i] = new DoubleAdder();
      }
    }
    private final Double[] upperBounds;
    private final DoubleAdder[] cumulativeCounts;
    private final DoubleAdder sum = new DoubleAdder();

    static TimeProvider timeProvider = new TimeProvider();
    /**
     * Observe the given amount.
     */
    public void observe(double amt) {
      for (int i = 0; i < upperBounds.length; ++i) {
        // The last bucket is +Inf, so we always increment.
        if (amt <= upperBounds[i]) {
          cumulativeCounts[i].add(1);
          break;
        }
      }
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
     * Get the value of the Histogram.
     * <p>
     * <em>Warning:</em> The definition of {@link Value} is subject to change.
     */
    public Value get() {
      double[] buckets = new double[cumulativeCounts.length];
      double acc = 0;
      for (int i = 0; i < cumulativeCounts.length; ++i) {
        acc += cumulativeCounts[i].sum();
        buckets[i] = acc;
      }
      return new Value(sum.sum(), buckets);
    }
  }

  // Convenience methods.
  /**
   * Observe the given amount on the histogram with no labels.
   */
  public void observe(double amt) {
    noLabelsChild.observe(amt);
  }
  /**
   * Start a timer to track a duration on the histogram with no labels.
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
      List<String> labelNamesWithLe = new ArrayList<String>(labelNames);
      labelNamesWithLe.add("le");
      for (int i = 0; i < v.buckets.length; ++i) {
        List<String> labelValuesWithLe = new ArrayList<String>(c.getKey());
        labelValuesWithLe.add(doubleToGoString(buckets[i]));
        samples.add(new MetricFamilySamples.Sample(fullname + "_bucket", labelNamesWithLe, labelValuesWithLe, v.buckets[i]));
      }
      samples.add(new MetricFamilySamples.Sample(fullname + "_count", labelNames, c.getKey(), v.buckets[buckets.length-1]));
      samples.add(new MetricFamilySamples.Sample(fullname + "_sum", labelNames, c.getKey(), v.sum));
    }

    MetricFamilySamples mfs = new MetricFamilySamples(fullname, Type.HISTOGRAM, help, samples);
    List<MetricFamilySamples> mfsList = new ArrayList<MetricFamilySamples>();
    mfsList.add(mfs);
    return mfsList;
  }

  Double[] getBuckets() {
    return buckets;
  }

  static class TimeProvider {
    long nanoTime() {
      return System.nanoTime();
    }
  }
}
