package io.prometheus.client.exemplars;

/**
 * Static configuration for Exemplar behavior.
 */
public class ExemplarConfig {

  private static volatile boolean enabled = true;
  private static volatile HistogramExemplarSampler histogramExemplarSampler;
  private static volatile CounterExemplarSampler counterExemplarSampler;

  static {
    ExemplarSampler defaultExemplarSampler = new Tracer().initExemplarSampler();
    counterExemplarSampler = defaultExemplarSampler;
    histogramExemplarSampler = defaultExemplarSampler;
  }

  /**
   * Set the default exemplar sampler for Counters.
   */
  public static void setCounterExemplarSampler(CounterExemplarSampler counterExemplarSampler) {
    ExemplarConfig.counterExemplarSampler = counterExemplarSampler;
  }

  /**
   * Set the default exemplar sampler for Histograms.
   */
  public static void setHistogramExemplarSampler(HistogramExemplarSampler histogramExemplarSampler) {
    ExemplarConfig.histogramExemplarSampler = histogramExemplarSampler;
  }

  /**
   * Prevent metrics from loading exemplars from an {@link ExemplarSampler} by default.
   * <p>
   * You can still enable individual metrics to load exemplars from an {@link ExemplarSampler} by calling the
   * metric builder's {@code withExemplars()} method, and you can still provide single exemplars explicitly
   * for individual observations with the {@code ...withExemplar()} methods.
   */
  public static void disableExemplars() {
    enabled = false;
  }

  /**
   * Allow metrics to load exemplars from an {@link ExemplarSampler} by default.
   * <p>
   * You can still disallow individual metrics to load exemplars from an {@link ExemplarSampler} by calling
   * the metric builder's {@code withoutExemplars()} method.
   * <p>
   * Exemplars are enabled by default. This method is here so that you can temporarily {@link #disableExemplars()}
   * and then {@link #enableExemplars()} again.
   */
  public static void enableExemplars() {
    enabled = true;
  }

  /**
   * @return the {@link CounterExemplarSampler} that is used by default in {@code Counter} metrics.
   */
  public static CounterExemplarSampler getCounterExemplarSampler() {
    return counterExemplarSampler;
  }

  /**
   * @return the {@link HistogramExemplarSampler} that is used by default in {@code Histogram} metrics.
   */
  public static HistogramExemplarSampler getHistogramExemplarSampler() {
    return histogramExemplarSampler;
  }

  /**
   * @return true by default, false if {@link #disableExemplars()} was called.
   */
  public static boolean isExemplarsEnabled() {
    return enabled;
  }
}
