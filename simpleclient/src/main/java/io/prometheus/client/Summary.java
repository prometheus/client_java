package io.prometheus.client;

import io.prometheus.client.CKMSQuantiles.Quantile;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

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
 *
 *     // Or if using Java 8 and lambdas.
 *     void processRequestLambda(Request req) {
 *       receivedBytes.observe(req.size());
 *       requestLatency.time(() -> {
 *         // Your code here.
 *       });
 *     }
 * }
 * }
 * </pre>
 * This would allow you to track request rate, average latency and average request size.
 *
 * <p>
 * How to add custom quantiles:
 * <pre>
 * {@code
 *     static final Summary myMetric = Summary.build()
 *             .quantile(0.5, 0.05)   // Add 50th percentile (= median) with 5% tolerated error
 *             .quantile(0.9, 0.01)   // Add 90th percentile with 1% tolerated error
 *             .quantile(0.99, 0.001) // Add 99th percentile with 0.1% tolerated error
 *             .name("requests_size_bytes")
 *             .help("Request size in bytes.")
 *             .register();
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
public class Summary extends SimpleCollector<Summary.Child> implements Counter.Describable {

  final List<Quantile> quantiles; // Can be empty, but can never be null.
  final long maxAgeSeconds;
  final int ageBuckets;

  Summary(Builder b) {
    super(b);
    quantiles = Collections.unmodifiableList(new ArrayList<Quantile>(b.quantiles));
    this.maxAgeSeconds = b.maxAgeSeconds;
    this.ageBuckets = b.ageBuckets;
    initializeNoLabelsChild();
  }

  public static class Builder extends SimpleCollector.Builder<Builder, Summary> {

    private final List<Quantile> quantiles = new ArrayList<Quantile>();
    private long maxAgeSeconds = TimeUnit.MINUTES.toSeconds(10);
    private int ageBuckets = 5;

    public Builder quantile(double quantile, double error) {
      if (quantile < 0.0 || quantile > 1.0) {
        throw new IllegalArgumentException("Quantile " + quantile + " invalid: Expected number between 0.0 and 1.0.");
      }
      if (error < 0.0 || error > 1.0) {
        throw new IllegalArgumentException("Error " + error + " invalid: Expected number between 0.0 and 1.0.");
      }
      quantiles.add(new Quantile(quantile, error));
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
    public Summary create() {
      for (String label : labelNames) {
        if (label.equals("quantile")) {
          throw new IllegalStateException("Summary cannot have a label named 'quantile'.");
        }
      }
      dontInitializeNoLabelsChild = true;
      return new Summary(this);
    }
  }

  /**
   *  Return a Builder to allow configuration of a new Summary. Ensures required fields are provided.
   *
   *  @param name The name of the metric
   *  @param help The help string of the metric
   */
  public static Builder build(String name, String help) {
    return new Builder().name(name).help(help);
  }

  /**
   *  Return a Builder to allow configuration of a new Summary.
   */
  public static Builder build() {
    return new Builder();
  }

  @Override
  protected Child newChild() {
    return new Child(quantiles, maxAgeSeconds, ageBuckets);
  }


  /**
   * Represents an event being timed.
   */
  public static class Timer implements Closeable {
    private final Child child;
    private final long start;
    private Timer(Child child, long start) {
      this.child = child;
      this.start = start;
    }
    /**
     * Observe the amount of time in seconds since {@link Child#startTimer} was called.
     * @return Measured duration in seconds since {@link Child#startTimer} was called.
     */
    public double observeDuration() {
      double elapsed = SimpleTimer.elapsedSecondsFromNanos(start, SimpleTimer.defaultTimeProvider.nanoTime());
      child.observe(elapsed);
      return elapsed;
    }

    /**
     * Equivalent to calling {@link #observeDuration()}.
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
      observeDuration();
    }
  }

  /**
   * The value of a single Summary.
   * <p>
   * <em>Warning:</em> References to a Child become invalid after using
   * {@link SimpleCollector#remove} or {@link SimpleCollector#clear}.
   */
  public static class Child {

    /**
     * Executes runnable code (i.e. a Java 8 Lambda) and observes a duration of how long it took to run.
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

    public static class Value {
      public final double count;
      public final double sum;
      public final SortedMap<Double, Double> quantiles;

      private Value(double count, double sum, List<Quantile> quantiles, TimeWindowQuantiles quantileValues) {
        this.count = count;
        this.sum = sum;
        this.quantiles = Collections.unmodifiableSortedMap(snapshot(quantiles, quantileValues));
      }

      private SortedMap<Double, Double> snapshot(List<Quantile> quantiles, TimeWindowQuantiles quantileValues) {
        SortedMap<Double, Double> result = new TreeMap<Double, Double>();
        for (Quantile q : quantiles) {
          result.put(q.quantile, quantileValues.get(q.quantile));
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
    private final List<Quantile> quantiles;
    private final TimeWindowQuantiles quantileValues;

    private Child(List<Quantile> quantiles, long maxAgeSeconds, int ageBuckets) {
      this.quantiles = quantiles;
      if (quantiles.size() > 0) {
        quantileValues = new TimeWindowQuantiles(quantiles.toArray(new Quantile[]{}), maxAgeSeconds, ageBuckets);
      } else {
        quantileValues = null;
      }
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
      return new Timer(this, SimpleTimer.defaultTimeProvider.nanoTime());
    }
    /**
     * Get the value of the Summary.
     * <p>
     * <em>Warning:</em> The definition of {@link Value} is subject to change.
     */
    public Value get() {
      return new Value(count.sum(), sum.sum(), quantiles, quantileValues);
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
   * Executes runnable code (i.e. a Java 8 Lambda) and observes a duration of how long it took to run.
   *
   * @param timeable Code that is being timed
   * @return Measured duration in seconds for timeable to complete.
   */
  public double time(Runnable timeable){
    return noLabelsChild.time(timeable);
  }
  
  /**
   * Get the value of the Summary.
   * <p>
   * <em>Warning:</em> The definition of {@link Child.Value} is subject to change.
   */
  public Child.Value get() {
    return noLabelsChild.get();
  }

  @Override
  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples.Sample> samples = new ArrayList<MetricFamilySamples.Sample>();
    for(Map.Entry<List<String>, Child> c: children.entrySet()) {
      Child.Value v = c.getValue().get();
      List<String> labelNamesWithQuantile = new ArrayList<String>(labelNames);
      labelNamesWithQuantile.add("quantile");
      for(Map.Entry<Double, Double> q : v.quantiles.entrySet()) {
        List<String> labelValuesWithQuantile = new ArrayList<String>(c.getKey());
        labelValuesWithQuantile.add(doubleToGoString(q.getKey()));
        samples.add(new MetricFamilySamples.Sample(fullname, labelNamesWithQuantile, labelValuesWithQuantile, q.getValue()));
      }
      samples.add(new MetricFamilySamples.Sample(fullname + "_count", labelNames, c.getKey(), v.count));
      samples.add(new MetricFamilySamples.Sample(fullname + "_sum", labelNames, c.getKey(), v.sum));
    }

    return familySamplesList(Type.SUMMARY, samples);
  }

  @Override
  public List<MetricFamilySamples> describe() {
    return Collections.<MetricFamilySamples>singletonList(new SummaryMetricFamily(fullname, help, labelNames));
  }

}
