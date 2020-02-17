package io.prometheus.client;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Summary metric using HdrHistogram, to track the size of events.
 * <p>
 * Example of uses for Summaries include:
 * <ul>
 *  <li>Response latency</li>
 *  <li>Request size</li>
 * </ul>
 *
 * See http://hdrhistogram.org and https://github.com/HdrHistogram/HdrHistogram for more info on HdrHistogram.
 *
 * <p>
 * Example Summaries:
 * <pre>
 * {@code
 *   class YourClass {
 *     static final HdrSummary receivedBytes = HdrSummary.build()
 *         .name("requests_size_bytes").help("Request size in bytes.").register();
 *     static final HdrSummary requestLatency = HdrSummary.build()
 *         .name("requests_latency_seconds").help("Request latency in seconds.").register();
 *
 *     void processRequest(Request req) {
 *       HdrSummary.Timer requestTimer = requestLatency.startTimer();
 *       try {
 *         // Your code here.
 *       } finally {
 *         receivedBytes.observe(req.size());
 *         requestTimer.observeDuration();
 *       }
 *     }
 *
 *     // Or if using Java 8 and lambdas.
 *     void processRequestLambda(Request req) {
 *       receivedBytes.observe(req.size());
 *       requestLatency.time(() -> {
 *         // Your code here.
 *       });
 *     }
 *   }
 * }
 * </pre>
 * This would allow you to track request rate, average latency and average request size.
 *
 * <p>
 * How to add custom quantiles:
 * <pre>
 * {@code
 *   static final HdrSummary myMetric = HdrSummary.build()
 *       .quantile(0.5)  // Add 50th percentile (= median)
 *       .quantile(0.9)  // Add 90th percentile
 *       .quantile(0.99) // Add 99th percentile
 *       .name("requests_size_bytes")
 *       .help("Request size in bytes.")
 *       .register();
 * }
 * </pre>
 *
 * The quantiles are calculated over a sliding window of time. There are two options to configure this time window:
 * <ul>
 *   <li>maxAgeSeconds(long): Set the duration of the time window is, i.e. how long observations are kept before they are discarded.
 *       Default is 10 minutes.
 *   <li>ageBuckets(int): Set the number of buckets used to implement the sliding time window. If your time window is 10 minutes, and you have ageBuckets=5,
 *       buckets will be switched every 2 minutes. The value is a trade-off between resources (memory and cpu for maintaining the bucket)
 *       and how smooth the time window is moved. Default value is 5.
 * </ul>
 *
 * See https://prometheus.io/docs/practices/histograms/ for more info on quantiles.
 */
public class HdrSummary extends SimpleCollector<HdrSummary.Child> implements Counter.Describable {

  private final List<Double> quantiles; // Can be empty, but can never be null.
  private final int numberOfSignificantValueDigits;
  private final long maxAgeSeconds;
  private final int ageBuckets;

  private HdrSummary(Builder b) {
    super(b);
    this.quantiles = Collections.unmodifiableList(new ArrayList<Double>(b.quantiles));
    this.numberOfSignificantValueDigits = b.numberOfSignificantValueDigits;
    this.maxAgeSeconds = b.maxAgeSeconds;
    this.ageBuckets = b.ageBuckets;
    initializeNoLabelsChild();
  }

  public static class Builder extends SimpleCollector.Builder<Builder, HdrSummary> {

    private final List<Double> quantiles = new ArrayList<Double>();
    private int numberOfSignificantValueDigits = 3;
    private long maxAgeSeconds = TimeUnit.MINUTES.toSeconds(10);
    private int ageBuckets = 5;

    public Builder quantile(double quantile) {
      if (quantile < 0.0 || quantile > 1.0) {
        throw new IllegalArgumentException("Quantile " + quantile + " invalid: Expected number between 0.0 and 1.0.");
      }
      quantiles.add(quantile);
      return this;
    }

    public Builder numberOfSignificantValueDigits(int numberOfSignificantValueDigits) {
      if (numberOfSignificantValueDigits < 0 || numberOfSignificantValueDigits > 5) {
        throw new IllegalArgumentException("numberOfSignificantValueDigits cannot be " + numberOfSignificantValueDigits);
      }
      this.numberOfSignificantValueDigits = numberOfSignificantValueDigits;
      return this;
    }

    public Builder maxAgeSeconds(long maxAgeSeconds) {
      if (maxAgeSeconds <= 0) {
        throw new IllegalArgumentException("maxAgeSeconds cannot be " + maxAgeSeconds);
      }
      this.maxAgeSeconds = maxAgeSeconds;
      return this;
    }

    public Builder ageBuckets(int ageBuckets) {
      if (ageBuckets <= 0) {
        throw new IllegalArgumentException("ageBuckets cannot be " + ageBuckets);
      }
      this.ageBuckets = ageBuckets;
      return this;
    }

    @Override
    public HdrSummary create() {
      for (String label : labelNames) {
        if (label.equals("quantile")) {
          throw new IllegalStateException("Summary cannot have a label named 'quantile'.");
        }
      }
      dontInitializeNoLabelsChild = true;
      return new HdrSummary(this);
    }

  }

  /**
   * Return a Builder to allow configuration of a new HdrSummary. Ensures required fields are provided.
   *
   * @param name The name of the metric
   * @param help The help string of the metric
   */
  public static Builder build(String name, String help) {
    return new Builder().name(name).help(help);
  }

  /**
   * Return a Builder to allow configuration of a new HdrSummary.
   */
  public static Builder build() {
    return new Builder();
  }

  @Override
  protected Child newChild() {
    return new Child(quantiles, numberOfSignificantValueDigits, maxAgeSeconds, ageBuckets);
  }

  /**
   * Represents an event being timed.
   */
  public static class Timer implements Closeable {

    private final Child child;
    private final long start;

    private Timer(Child child) {
      this.child = child;
      this.start = SimpleTimer.defaultTimeProvider.nanoTime();
    }

    /**
     * Observe the amount of time in seconds since {@link Child#startTimer} was called.
     *
     * @return Measured duration in seconds since {@link Child#startTimer} was called.
     */
    public double observeDuration() {
      long end = SimpleTimer.defaultTimeProvider.nanoTime();
      double elapsed = SimpleTimer.elapsedSecondsFromNanos(start, end);
      child.observe(elapsed);
      return elapsed;
    }

    /**
     * Equivalent to calling {@link #observeDuration()}.
     */
    @Override
    public void close() {
      observeDuration();
    }

  }

  /**
   * The value of a single HdrSummary.
   * <p>
   * <em>Warning:</em> References to a Child become invalid after using
   * {@link SimpleCollector#remove} or {@link SimpleCollector#clear}.
   */
  public static class Child {

    public static class Value {

      public final double count;
      public final double sum;
      public final double min;
      public final double max;
      public final SortedMap<Double, Double> quantiles;

      private Value(DoubleAdder count, DoubleAdder sum, List<Double> quantiles, HdrTimeWindowQuantiles quantileValues) {
        this.count = count.sum();
        this.sum = sum.sum();
        this.min = quantileValues == null ? Double.NaN : quantileValues.getMin();
        this.max = quantileValues == null ? Double.NaN : quantileValues.getMax();
        this.quantiles = Collections.unmodifiableSortedMap(snapshot(quantiles, quantileValues));
      }

      private SortedMap<Double, Double> snapshot(List<Double> quantiles, HdrTimeWindowQuantiles quantileValues) {
        SortedMap<Double, Double> result = new TreeMap<Double, Double>();
        for (Double quantile : quantiles) {
          result.put(quantile, quantileValues.get(quantile));
        }
        return result;
      }

    }

    // Having these separate leaves us open to races,
    // however Prometheus as whole has other races
    // that mean adding atomicity here wouldn't be useful.
    // This should be reevaluated in the future.
    private final DoubleAdder count = new DoubleAdder();
    private final DoubleAdder sum = new DoubleAdder();
    private final List<Double> quantiles;
    private final HdrTimeWindowQuantiles quantileValues;

    private Child(List<Double> quantiles, int numberOfSignificantValueDigits, long maxAgeSeconds, int ageBuckets) {
      this.quantiles = quantiles;
      this.quantileValues = quantiles.isEmpty() ? null : new HdrTimeWindowQuantiles(numberOfSignificantValueDigits, maxAgeSeconds, ageBuckets);
    }

    /**
     * Observe the given amount.
     */
    public void observe(double amt) {
      count.add(1);
      sum.add(amt);
      if (quantileValues != null) {
        quantileValues.insert(amt);
      }
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
     * Executes runnable code (e.g. a Java 8 Lambda) and observes a duration of how long it took to run.
     *
     * @param timeable Code that is being timed
     * @return Measured duration in seconds for timeable to complete.
     */
    public double time(Runnable timeable) {
      Timer timer = startTimer();
      double elapsed;
      try {
        timeable.run();
      } finally {
        elapsed = timer.observeDuration();
      }
      return elapsed;
    }

    /**
     * Executes callable code (e.g. a Java 8 Lambda) and observes a duration of how long it took to run.
     *
     * @param timeable Code that is being timed
     * @return Result returned by callable.
     */
    public <E> E time(Callable<E> timeable) {
      Timer timer = startTimer();
      try {
        return timeable.call();
      } catch (Exception e) {
        throw new RuntimeException(e);
      } finally {
        timer.observeDuration();
      }
    }

    /**
     * Get the value of the Summary.
     * <p>
     * <em>Warning:</em> The definition of {@link Value} is subject to change.
     */
    public Value get() {
      return new Value(count, sum, quantiles, quantileValues);
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

  /**
   * Executes runnable code (e.g. a Java 8 Lambda) and observes a duration of how long it took to run.
   *
   * @param timeable Code that is being timed
   * @return Measured duration in seconds for timeable to complete.
   */
  public double time(Runnable timeable) {
    return noLabelsChild.time(timeable);
  }

  /**
   * Executes callable code (e.g. a Java 8 Lambda) and observes a duration of how long it took to run.
   *
   * @param timeable Code that is being timed
   * @return Result returned by callable.
   */
  public <E> E time(Callable<E> timeable) {
    return noLabelsChild.time(timeable);
  }

  /**
   * Get the value of the HdrSummary.
   * <p>
   * <em>Warning:</em> The definition of {@link Child.Value} is subject to change.
   */
  public Child.Value get() {
    return noLabelsChild.get();
  }

  @Override
  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples.Sample> samples = new ArrayList<MetricFamilySamples.Sample>();
    for (Map.Entry<List<String>, Child> child : children.entrySet()) {
      Child.Value value = child.getValue().get();
      List<String> labelNamesWithQuantile = new ArrayList<String>(labelNames);
      labelNamesWithQuantile.add("quantile");
      for (Map.Entry<Double, Double> quantile : value.quantiles.entrySet()) {
        List<String> labelValuesWithQuantile = new ArrayList<String>(child.getKey());
        labelValuesWithQuantile.add(doubleToGoString(quantile.getKey()));
        samples.add(new MetricFamilySamples.Sample(fullname, labelNamesWithQuantile, labelValuesWithQuantile, quantile.getValue()));
      }
      if (!value.quantiles.isEmpty()) {
        samples.add(new MetricFamilySamples.Sample(fullname + "_min", labelNames, child.getKey(), value.min));
        samples.add(new MetricFamilySamples.Sample(fullname + "_max", labelNames, child.getKey(), value.max));
      }
      samples.add(new MetricFamilySamples.Sample(fullname + "_count", labelNames, child.getKey(), value.count));
      samples.add(new MetricFamilySamples.Sample(fullname + "_sum", labelNames, child.getKey(), value.sum));
    }
    return familySamplesList(Type.SUMMARY, samples);
  }

  @Override
  public List<MetricFamilySamples> describe() {
    return Collections.<MetricFamilySamples>singletonList(new SummaryMetricFamily(fullname, help, labelNames));
  }

}
