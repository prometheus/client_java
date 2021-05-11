package io.prometheus.client.exemplars;

/**
 * Static configuration class for Exemplar behavior.
 */
public class ExemplarConfig {

  private static final ExemplarSampler noopExemplarSampler = new NoopExemplarSampler();

  private static volatile boolean enabled = true;
  private static volatile HistogramExemplarSampler histogramExemplarSampler;
  private static volatile CounterExemplarSampler counterExemplarSampler;

  static {
    ExemplarSampler defaultExemplarSampler = new Tracer().initExemplarSampler(noopExemplarSampler);
    counterExemplarSampler = defaultExemplarSampler;
    histogramExemplarSampler = defaultExemplarSampler;
  }

  /**
   * Set the exemplar sampler used when building Counters.
   * <p>
   * This must be called before a Counter is created.
   *
   * @param counterExemplarSampler will be used by default when creating new {@code Counter} metrics,
   *                               unless {@link #disableExemplarSamplers()} was called.
   */
  public static void setCounterExemplarSampler(CounterExemplarSampler counterExemplarSampler) {
    if (counterExemplarSampler == null) {
      throw new NullPointerException();
    }
    ExemplarConfig.counterExemplarSampler = counterExemplarSampler;
  }

  /**
   * Set the exemplar sampler used when building Histograms.
   * <p>
   * This must be called before a Histogram is created.
   *
   * @param histogramExemplarSampler will be used by default when creating new {@code Histogram} metrics,
   *                                 unless {@link #disableExemplarSamplers()} was called.
   */
  public static void setHistogramExemplarSampler(HistogramExemplarSampler histogramExemplarSampler) {
    if (histogramExemplarSampler == null) {
      throw new NullPointerException();
    }
    ExemplarConfig.histogramExemplarSampler = histogramExemplarSampler;
  }

  /**
   * Disable the implicit exemplar samplers by default.
   * <p>
   * Exemplars can still be enabled for individual metrics in the metric builder,
   * or they can be provided explicitly for individual observations with the {@code ...withExemplar()} methods.
   * <p>
   * This must be called before a Counter or a Histogram is created.
   */
  public static void disableExemplarSamplers() {
    enabled = false;
  }

  /**
   * @return the {@link CounterExemplarSampler} that is used by default when creating new {@code Counter} metrics.
   */
  public static CounterExemplarSampler getCounterExemplarSampler() {
    return counterExemplarSampler;
  }

  /**
   * @return the {@link HistogramExemplarSampler} that is used by default when creating new {@code Histogram} metrics.
   */
  public static HistogramExemplarSampler getHistogramExemplarSampler() {
    return histogramExemplarSampler;
  }

  /**
   * @return the {@link ExemplarSampler} used when the exemplar sampler is disabled.
   */
  public static ExemplarSampler getNoopExemplarSampler() {
    return noopExemplarSampler;
  }

  /**
   * @return true by default, false if {@link #disableExemplarSamplers()} was called.
   */
  public static boolean isExemplarSamplerEnabled() {
    return enabled;
  }
}
