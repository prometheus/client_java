package io.prometheus.client;

import io.prometheus.client.exemplars.Exemplar;
import io.prometheus.client.exemplars.ExemplarConfig;
import io.prometheus.client.exemplars.HistogramExemplarSampler;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

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
 *
 *     // Or if using Java 8 lambdas.
 *     void processRequestLambda(Request req) {
 *        requestLatency.time(() -> {
 *          // Your code here.
 *        });
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
public class Histogram extends SimpleCollector<Histogram.Child> implements Collector.Describable {
  private final double[] buckets;
  private final Boolean exemplarsEnabled; // null means default from ExemplarConfig applies
  private final HistogramExemplarSampler exemplarSampler;

  Histogram(Builder b) {
    super(b);
    this.exemplarsEnabled = b.exemplarsEnabled;
    this.exemplarSampler = b.exemplarSampler;
    buckets = b.buckets;
    initializeNoLabelsChild();
  }

  public static class Builder extends SimpleCollector.Builder<Builder, Histogram> {

    private Boolean exemplarsEnabled = null;
    private HistogramExemplarSampler exemplarSampler = null;
    private double[] buckets = new double[] { .005, .01, .025, .05, .075, .1, .25, .5, .75, 1, 2.5, 5, 7.5, 10 };

    @Override
    public Histogram create() {
      for (int i = 0; i < buckets.length - 1; i++) {
        if (buckets[i] >= buckets[i + 1]) {
          throw new IllegalStateException("Histogram buckets must be in increasing order: "
              + buckets[i] + " >= " + buckets[i + 1]);
        }
      }
      if (buckets.length == 0) {
        throw new IllegalStateException("Histogram must have at least one bucket.");
      }
      for (String label : labelNames) {
        if (label.equals("le")) {
          throw new IllegalStateException("Histogram cannot have a label named 'le'.");
        }
      }

      // Append infinity bucket if it's not already there.
      if (buckets[buckets.length - 1] != Double.POSITIVE_INFINITY) {
        double[] tmp = new double[buckets.length + 1];
        System.arraycopy(buckets, 0, tmp, 0, buckets.length);
        tmp[buckets.length] = Double.POSITIVE_INFINITY;
        buckets = tmp;
      }
      dontInitializeNoLabelsChild = true;
      return new Histogram(this);
    }

    /**
     * Set the upper bounds of buckets for the histogram.
     */
    public Builder buckets(double... buckets) {
      this.buckets = buckets;
      return this;
    }

    /**
     * Set the upper bounds of buckets for the histogram with a linear sequence.
     */
    public Builder linearBuckets(double start, double width, int count) {
      buckets = new double[count];
      for (int i = 0; i < count; i++) {
        buckets[i] = start + i * width;
      }
      return this;
    }

    /**
     * Set the upper bounds of buckets for the histogram with an exponential sequence.
     */
    public Builder exponentialBuckets(double start, double factor, int count) {
      buckets = new double[count];
      for (int i = 0; i < count; i++) {
        buckets[i] = start * Math.pow(factor, i);
      }
      return this;
    }

    /**
     * Enable exemplars and provide a custom {@link HistogramExemplarSampler}.
     */
    public Builder withExemplarSampler(HistogramExemplarSampler exemplarSampler) {
      if (exemplarSampler == null) {
        throw new NullPointerException();
      }
      this.exemplarSampler = exemplarSampler;
      return withExemplars();
    }

    /**
     * Allow this histogram to load exemplars from a {@link HistogramExemplarSampler}.
     * <p>
     * If a specific exemplar sampler is configured for this histogram that exemplar sampler is used
     * (see {@link #withExemplarSampler(HistogramExemplarSampler)}).
     * Otherwise the default from {@link ExemplarConfig} is used.
     */
    public Builder withExemplars() {
      this.exemplarsEnabled = TRUE;
      return this;
    }

    /**
     * Prevent this histogram from loading exemplars from a {@link HistogramExemplarSampler}.
     * <p>
     * You can still provide exemplars for explicitly individual observations, e.g. using
     * {@link #observeWithExemplar(double, String...)}.
     */
    public Builder withoutExemplars() {
      this.exemplarsEnabled = FALSE;
      return this;
    }
  }

  /**
   * Return a Builder to allow configuration of a new Histogram. Ensures required fields are provided.
   *
   * @param name The name of the metric
   * @param help The help string of the metric
   */
  public static Builder build(String name, String help) {
    return new Builder().name(name).help(help);
  }

  /**
   * Return a Builder to allow configuration of a new Histogram.
   */
  public static Builder build() {
    return new Builder();
  }

  @Override
  protected Child newChild() {
    return new Child(buckets, exemplarsEnabled, exemplarSampler);
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
     *
     * @return Measured duration in seconds since {@link Child#startTimer} was called.
     */
    public double observeDuration() {
      return observeDurationWithExemplar((String[]) null);
    }

    public double observeDurationWithExemplar(String... exemplarLabels) {
      double elapsed = SimpleTimer.elapsedSecondsFromNanos(start, SimpleTimer.defaultTimeProvider.nanoTime());
      child.observeWithExemplar(elapsed, exemplarLabels);
      return elapsed;
    }

    public double observeDurationWithExemplar(Map<String, String> exemplarLabels) {
      return observeDurationWithExemplar(Exemplar.mapToArray(exemplarLabels));
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
   * The value of a single Histogram.
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
      return timeWithExemplar(timeable, (String[]) null);
    }

    /**
     * Like {@link #time(Runnable)}, but additionally create an exemplar.
     * <p>
     * See {@link #observeWithExemplar(double, String...)}  for documentation on the {@code exemplarLabels} parameter.
     */
    public double timeWithExemplar(Runnable timeable, String... exemplarLabels) {
      Timer timer = startTimer();

      double elapsed;
      try {
        timeable.run();
      } finally {
        elapsed = timer.observeDurationWithExemplar(exemplarLabels);
      }
      return elapsed;
    }

    /**
     * Like {@link #time(Runnable)}, but additionally create an exemplar.
     * <p>
     * See {@link #observeWithExemplar(double, Map)}  for documentation on the {@code exemplarLabels} parameter.
     */
    public double timeWithExemplar(Runnable timeable, Map<String, String> exemplarLabels) {
      return timeWithExemplar(timeable, Exemplar.mapToArray(exemplarLabels));
    }

    /**
     * Executes callable code (e.g. a Java 8 Lambda) and observes a duration of how long it took to run.
     *
     * @param timeable Code that is being timed
     * @return Result returned by callable.
     */
    public <E> E time(Callable<E> timeable) {
      return timeWithExemplar(timeable, (String[]) null);
    }

    /**
     * Like {@link #time(Callable)}, but additionally create an exemplar.
     * <p>
     * See {@link #observeWithExemplar(double, String...)}  for documentation on the {@code exemplarLabels} parameter.
     */
    public <E> E timeWithExemplar(Callable<E> timeable, String... exemplarLabels) {
      Timer timer = startTimer();

      try {
        return timeable.call();
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new RuntimeException(e);
      } finally {
        timer.observeDurationWithExemplar(exemplarLabels);
      }
    }

    /**
     * Like {@link #time(Callable)}, but additionally create an exemplar.
     * <p>
     * See {@link #observeWithExemplar(double, Map)}  for documentation on the {@code exemplarLabels} parameter.
     */
    public <E> E timeWithExemplar(Callable<E> timeable, Map<String, String> exemplarLabels) {
      return timeWithExemplar(timeable, Exemplar.mapToArray(exemplarLabels));
    }

    public static class Value {
      public final double sum;
      public final double[] buckets;
      public final Exemplar[] exemplars;
      public final long created;

      public Value(double sum, double[] buckets, Exemplar[] exemplars, long created) {
        this.sum = sum;
        this.buckets = buckets;
        this.exemplars = exemplars;
        this.created = created;
      }
    }

    private Child(double[] buckets, Boolean exemplarsEnabled, HistogramExemplarSampler exemplarSampler) {
      upperBounds = buckets;
      this.exemplarsEnabled = exemplarsEnabled;
      this.exemplarSampler = exemplarSampler;
      exemplars = new ArrayList<AtomicReference<Exemplar>>(buckets.length);
      cumulativeCounts = new DoubleAdder[buckets.length];
      for (int i = 0; i < buckets.length; ++i) {
        cumulativeCounts[i] = new DoubleAdder();
        exemplars.add(new AtomicReference<Exemplar>());
      }
    }

    private final ArrayList<AtomicReference<Exemplar>> exemplars;
    private final Boolean exemplarsEnabled;
    private final HistogramExemplarSampler exemplarSampler;
    private final double[] upperBounds;
    private final DoubleAdder[] cumulativeCounts;
    private final DoubleAdder sum = new DoubleAdder();
    private final long created = System.currentTimeMillis();

    /**
     * Observe the given amount.
     *
     * @param amt in most cases amt should be &gt;= 0. Negative values are supported, but you should read
     *            <a href="https://prometheus.io/docs/practices/histograms/#count-and-sum-of-observations">
     *            https://prometheus.io/docs/practices/histograms/#count-and-sum-of-observations</a> for
     *            implications and alternatives.
     */
    public void observe(double amt) {
      observeWithExemplar(amt, (String[]) null);
    }

    /**
     * Like {@link #observe(double)}, but additionally creates an exemplar.
     * <p>
     * This exemplar takes precedence over any exemplar returned by the {@link HistogramExemplarSampler} configured
     * in {@link ExemplarConfig}.
     * <p>
     * The exemplar will have {@code amt} as the value, {@code System.currentTimeMillis()} as the timestamp,
     * and the specified labels.
     *
     * @param amt            same as in {@link #observe(double)} (double)}
     * @param exemplarLabels list of name/value pairs, as documented in {@link Exemplar#Exemplar(double, String...)}.
     *                       A commonly used name is {@code "trace_id"}.
     *                       Calling {@code observeWithExemplar(amt)} means that an exemplar without labels is created.
     *                       Calling {@code observeWithExemplar(amt, (String[]) null)} is equivalent
     *                       to calling {@code observe(amt)}.
     */
    public void observeWithExemplar(double amt, String... exemplarLabels) {
      Exemplar exemplar = exemplarLabels == null ? null : new Exemplar(amt, System.currentTimeMillis(), exemplarLabels);
      for (int i = 0; i < upperBounds.length; ++i) {
        // The last bucket is +Inf, so we always increment.
        if (amt <= upperBounds[i]) {
          cumulativeCounts[i].add(1);
          updateExemplar(amt, i, exemplar);
          break;
        }
      }
      sum.add(amt);
    }

    /**
     * Like {@link #observeWithExemplar(double, String...)}, but the exemplar labels are passed as a {@link Map}.
     */
    public void observeWithExemplar(double amt, Map<String, String> exemplarLabels) {
      observeWithExemplar(amt, Exemplar.mapToArray(exemplarLabels));
    }

    private void updateExemplar(double amt, int i, Exemplar userProvidedExemplar) {
      AtomicReference<Exemplar> exemplar = exemplars.get(i);
      double bucketFrom = i == 0 ? Double.NEGATIVE_INFINITY : upperBounds[i - 1];
      double bucketTo = upperBounds[i];
      Exemplar prev, next;
      do {
        prev = exemplar.get();
        if (userProvidedExemplar != null) {
          next = userProvidedExemplar;
        } else {
          next = sampleNextExemplar(amt, bucketFrom, bucketTo, prev);
        }
        if (next == null || next == prev) {
          return;
        }
      } while (!exemplar.compareAndSet(prev, next));
    }

    private Exemplar sampleNextExemplar(double amt, double bucketFrom, double bucketTo, Exemplar prev) {
      if (FALSE.equals(exemplarsEnabled)) {
        return null;
      }
      if (exemplarSampler != null) {
        return exemplarSampler.sample(amt, bucketFrom, bucketTo, prev);
      }
      if (TRUE.equals(exemplarsEnabled) || ExemplarConfig.isExemplarsEnabled()) {
        HistogramExemplarSampler exemplarSampler = ExemplarConfig.getHistogramExemplarSampler();
        if (exemplarSampler != null) {
          return exemplarSampler.sample(amt, bucketFrom, bucketTo, prev);
        }
      }
      return null;
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
     * Get the value of the Histogram.
     * <p>
     * <em>Warning:</em> The definition of {@link Value} is subject to change.
     */
    public Value get() {
      double[] buckets = new double[cumulativeCounts.length];
      Exemplar[] exemplars = new Exemplar[cumulativeCounts.length];
      double acc = 0;
      for (int i = 0; i < cumulativeCounts.length; ++i) {
        acc += cumulativeCounts[i].sum();
        buckets[i] = acc;
        exemplars[i] = this.exemplars.get(i).get();
      }
      return new Value(sum.sum(), buckets, exemplars, created);
    }
  }

  // Convenience methods.

  /**
   * Observe the given amount on the histogram with no labels.
   *
   * @param amt in most cases amt should be &gt;= 0. Negative values are supported, but you should read
   *            <a href="https://prometheus.io/docs/practices/histograms/#count-and-sum-of-observations">
   *            https://prometheus.io/docs/practices/histograms/#count-and-sum-of-observations</a> for
   *            implications and alternatives.
   */
  public void observe(double amt) {
    noLabelsChild.observe(amt);
  }

  /**
   * Like {@link Child#observeWithExemplar(double, String...)}, but for the histogram without labels.
   */
  public void observeWithExemplar(double amt, String... exemplarLabels) {
    noLabelsChild.observeWithExemplar(amt, exemplarLabels);
  }

  /**
   * Like {@link Child#observeWithExemplar(double, Map)}, but for the histogram without labels.
   */
  public void observeWithExemplar(double amt, Map<String, String> exemplarLabels) {
    noLabelsChild.observeWithExemplar(amt, exemplarLabels);
  }

  /**
   * Start a timer to track a duration on the histogram with no labels.
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
   * Like {@link #time(Runnable)}, but additionally create an exemplar.
   * <p>
   * See {@link Child#observeWithExemplar(double, String...)} for documentation on the {@code exemplarLabels} parameter.
   */
  public double timeWithExemplar(Runnable timeable, String... exemplarLabels) {
    return noLabelsChild.timeWithExemplar(timeable, exemplarLabels);
  }

  /**
   * Like {@link #time(Runnable)}, but additionally create an exemplar.
   * <p>
   * See {@link Child#observeWithExemplar(double, Map)} for documentation on the {@code exemplarLabels} parameter.
   */
  public double timeWithExemplar(Runnable timeable, Map<String, String> exemplarLabels) {
    return noLabelsChild.timeWithExemplar(timeable, exemplarLabels);
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
   * Like {@link #time(Callable)}, but additionally create an exemplar.
   * <p>
   * See {@link Child#observeWithExemplar(double, String...)} for documentation on the {@code exemplarLabels} parameter.
   */
  public <E> E timeWithExemplar(Callable<E> timeable, String... exemplarLabels) {
    return noLabelsChild.timeWithExemplar(timeable, exemplarLabels);
  }

  /**
   * Like {@link #time(Callable)}, but additionally create an exemplar.
   * <p>
   * See {@link Child#observeWithExemplar(double, Map)} for documentation on the {@code exemplarLabels} parameter.
   */
  public <E> E timeWithExemplar(Callable<E> timeable, Map<String, String> exemplarLabels) {
    return noLabelsChild.timeWithExemplar(timeable, exemplarLabels);
  }

  @Override
  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples.Sample> samples = new ArrayList<MetricFamilySamples.Sample>();
    for (Map.Entry<List<String>, Child> c : children.entrySet()) {
      Child.Value v = c.getValue().get();
      List<String> labelNamesWithLe = new ArrayList<String>(labelNames);
      labelNamesWithLe.add("le");
      for (int i = 0; i < v.buckets.length; ++i) {
        List<String> labelValuesWithLe = new ArrayList<String>(c.getKey());
        labelValuesWithLe.add(doubleToGoString(buckets[i]));
        samples.add(new MetricFamilySamples.Sample(fullname + "_bucket", labelNamesWithLe, labelValuesWithLe, v.buckets[i], v.exemplars[i]));
      }
      samples.add(new MetricFamilySamples.Sample(fullname + "_count", labelNames, c.getKey(), v.buckets[buckets.length-1]));
      samples.add(new MetricFamilySamples.Sample(fullname + "_sum", labelNames, c.getKey(), v.sum));
      if (Environment.includeCreatedSeries()) {
        samples.add(new MetricFamilySamples.Sample(fullname + "_created", labelNames, c.getKey(), v.created / 1000.0));
      }
    }

    return familySamplesList(Type.HISTOGRAM, samples);
  }

  @Override
  public List<MetricFamilySamples> describe() {
    return Collections.singletonList(
        new MetricFamilySamples(fullname, Type.HISTOGRAM, help, Collections.<MetricFamilySamples.Sample>emptyList()));
  }

  double[] getBuckets() {
    return buckets;
  }
}