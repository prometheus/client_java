package io.prometheus.client.exemplars;

/**
 * Static configuration class for Exemplar behavior.
 */
public class ExemplarConfig {

  private static final ExemplarConfig instance = new ExemplarConfig();

  private final ExemplarSampler noopExemplarSampler;
  private final ExemplarSampler defaultExemplarSampler;

  private volatile HistogramExemplarSampler histogramExemplarSampler;
  private volatile CounterExemplarSampler counterExemplarSampler;

  private ExemplarConfig() {
    noopExemplarSampler = new NoopExemplarSampler();
    defaultExemplarSampler = new Tracer().initExemplarSampler(noopExemplarSampler);
    counterExemplarSampler = defaultExemplarSampler;
    histogramExemplarSampler = defaultExemplarSampler;
  }

  /**
   * @param counterExemplarSampler will be used by default when creating new {@code Counter} metrics.
   */
  public static void setCounterExemplarSampler(CounterExemplarSampler counterExemplarSampler) {
    if (counterExemplarSampler == null) {
      throw new NullPointerException();
    }
    instance.counterExemplarSampler = counterExemplarSampler;
  }

  /**
   * @param histogramExemplarSampler will be used by default when creating new {@code Histogram} metrics.
   */
  public static void setHistogramExemplarSampler(HistogramExemplarSampler histogramExemplarSampler) {
    if (histogramExemplarSampler == null) {
      throw new NullPointerException();
    }
    instance.histogramExemplarSampler = histogramExemplarSampler;
  }

  /**
   * Disable exemplars by default. Exemplars can still be enabled for individual metrics in the metric's builder.
   */
  public static void disableExemplars() {
    instance.counterExemplarSampler = instance.noopExemplarSampler;
    instance.histogramExemplarSampler = instance.noopExemplarSampler;
  }

  /**
   * @return the {@link CounterExemplarSampler} that is used by default when creating new {@code Counter} metrics.
   */
  public static CounterExemplarSampler getCounterExemplarSampler() {
    return instance.counterExemplarSampler;
  }

  /**
   * @return the {@link HistogramExemplarSampler} that is used by default when creating new {@code Histogram} metrics.
   */
  public static HistogramExemplarSampler getHistogramExemplarSampler() {
    return instance.histogramExemplarSampler;
  }

  /**
   * @return an {@link ExemplarSampler} that will never sample an exemplar.
   */
  public static ExemplarSampler getNoopExemplarSampler() {
    return instance.noopExemplarSampler;
  }

  /**
   * @return the default implementation of an {@link ExemplarSampler}. This implementation will sample a new Exemplar
   * if no previous Exemplar is present or if the previous Exemplar is older than ca 7 seconds.
   */
  public static ExemplarSampler getDefaultExemplarSampler() {
    return instance.defaultExemplarSampler;
  }
}
