package io.prometheus.client;

import io.prometheus.client.CKMSQuantiles.Quantile;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * {@link Summary} metrics and {@link Histogram} metrics can both be used to monitor latencies (or other things like request sizes).
 * <p>
 * An overview of when to use Summaries and when to use Histograms can be found on <a href="https://prometheus.io/docs/practices/histograms">https://prometheus.io/docs/practices/histograms</a>.
 * <p>
 * The following example shows how to measure latencies and request sizes:
 *
 * <pre>
 * class YourClass {
 *
 *   private static final Summary requestLatency = Summary.build()
 *       .name("requests_latency_seconds")
 *       .help("request latency in seconds")
 *       .register();
 *
 *   private static final Summary receivedBytes = Summary.build()
 *       .name("requests_size_bytes")
 *       .help("request size in bytes")
 *       .register();
 *
 *   public void processRequest(Request req) {
 *     Summary.Timer requestTimer = requestLatency.startTimer();
 *     try {
 *       // Your code here.
 *     } finally {
 *       requestTimer.observeDuration();
 *       receivedBytes.observe(req.size());
 *     }
 *   }
 * }
 * </pre>
 *
 * The {@link Summary} class provides different utility methods for observing values, like {@link #observe(double)},
 * {@link #startTimer()} and {@link Timer#observeDuration()}, {@link #time(Callable)}, etc.
 * <p>
 * By default, {@link Summary} metrics provide the <tt>count</tt> and the <tt>sum</tt>. For example, if you measure
 * latencies of a REST service, the <tt>count</tt> will tell you how often the REST service was called,
 * and the <tt>sum</tt> will tell you the total aggregated response time.
 * You can calculate the average response time using a Prometheus query dividing <tt>sum / count</tt>.
 * <p>
 * In addition to <tt>count</tt> and <tt>sum</tt>, you can configure a Summary to provide quantiles:
 *
 * <pre>
 * Summary requestLatency = Summary.build()
 *     .name("requests_latency_seconds")
 *     .help("Request latency in seconds.")
 *     .quantile(0.5, 0.01)    // 0.5 quantile (median) with 0.01 allowed error
 *     .quantile(0.95, 0.005)  // 0.95 quantile with 0.005 allowed error
 *     // ...
 *     .register();
 * </pre>
 *
 * As an example, a 0.95 quantile of 120ms tells you that 95% of the calls were faster than 120ms, and 5% of the calls were slower than 120ms.
 * <p>
 * Tracking exact quantiles require a large amount of memory, because all observations need to be stored in a sorted list. Therefore, we allow an error to significantly reduce memory usage.
 * <p>
 * In the example, the allowed error of 0.005 means that you will not get the exact 0.95 quantile, but anything between the 0.945 quantile and the 0.955 quantile.
 * <p>
 * Experiments show that the {@link Summary} typically needs to keep less than 100 samples to provide that precision, even if you have hundreds of millions of observations.
 * <p>
 * There are a few special cases:
 *
 * <ul>
 *   <li>You can set an allowed error of 0, but then the {@link Summary} will keep all observations in memory.</li>
 *   <li>You can track the minimum value with <tt>.quantile(0.0, 0.0)</tt>.
 *       This special case will not use additional memory even though the allowed error is 0.</li>
 *   <li>You can track the maximum value with <tt>.quantile(1.0, 0.0)</tt>.
 *       This special case will not use additional memory even though the allowed error is 0.</li>
 * </ul>
 *
 * Typically, you don't want to have a {@link Summary} representing the entire runtime of the application,
 * but you want to look at a reasonable time interval. {@link Summary} metrics implement a configurable sliding
 * time window:
 *
 * <pre>
 * Summary requestLatency = Summary.build()
 *     .name("requests_latency_seconds")
 *     .help("Request latency in seconds.")
 *     .maxAgeSeconds(10 * 60)
 *     .ageBuckets(5)
 *     // ...
 *     .register();
 * </pre>
 *
 * The default is a time window of 10 minutes and 5 age buckets, i.e. the time window is 10 minutes wide, and
 * we slide it forward every 2 minutes.
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

    /**
     * The class JavaDoc for {@link Summary} has more information on {@link #quantile(double, double)}.
     * @see Summary
     */
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

    /**
     * The class JavaDoc for {@link Summary} has more information on {@link #maxAgeSeconds(long)} 
     * @see Summary
     */
    public Builder maxAgeSeconds(long maxAgeSeconds) {
      if (maxAgeSeconds <= 0) {
        throw new IllegalArgumentException("maxAgeSeconds cannot be " + maxAgeSeconds);
      }
      this.maxAgeSeconds = maxAgeSeconds;
      return this;
    }

    /**
     * The class JavaDoc for {@link Summary} has more information on {@link #ageBuckets(int)} 
     * @see Summary
     */
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
     */
    @Override
    public void close() {
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
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new RuntimeException(e);
      } finally {
        timer.observeDuration();
      }
    }

    public static class Value {
      public final double count;
      public final double sum;
      public final SortedMap<Double, Double> quantiles;
      public final long created;

      private Value(double count, double sum, List<Quantile> quantiles, TimeWindowQuantiles quantileValues, long created) {
        this.count = count;
        this.sum = sum;
        this.quantiles = Collections.unmodifiableSortedMap(snapshot(quantiles, quantileValues));
        this.created = created;
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
    private final long created = System.currentTimeMillis();

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
     * @param amt in most cases amt should be &gt;= 0. Negative values are supported, but you should read
     *            <a href="https://prometheus.io/docs/practices/histograms/#count-and-sum-of-observations">
     *            https://prometheus.io/docs/practices/histograms/#count-and-sum-of-observations</a> for
     *            implications and alternatives.
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
      return new Value(count.sum(), sum.sum(), quantiles, quantileValues, created);
    }
  }

  // Convenience methods.
  /**
   * Observe the given amount on the summary with no labels.
   * @param amt in most cases amt should be &gt;= 0. Negative values are supported, but you should read
   *            <a href="https://prometheus.io/docs/practices/histograms/#count-and-sum-of-observations">
   *            https://prometheus.io/docs/practices/histograms/#count-and-sum-of-observations</a> for
   *            implications and alternatives.
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
  public double time(Runnable timeable){
    return noLabelsChild.time(timeable);
  }

  /**
   * Executes callable code (e.g. a Java 8 Lambda) and observes a duration of how long it took to run.
   *
   * @param timeable Code that is being timed
   * @return Result returned by callable.
   */
  public <E> E time(Callable<E> timeable){
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
      samples.add(new MetricFamilySamples.Sample(fullname + "_created", labelNames, c.getKey(), v.created / 1000.0));
    }

    return familySamplesList(Type.SUMMARY, samples);
  }

  @Override
  public List<MetricFamilySamples> describe() {
    return Collections.<MetricFamilySamples>singletonList(new SummaryMetricFamily(fullname, help, labelNames));
  }

}
