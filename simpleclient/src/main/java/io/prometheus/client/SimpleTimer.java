package io.prometheus.client;

/**
 * SimpleTimer, to measure elapsed duration in seconds as a double.
 *
 * <p>
 * This is a helper class intended to measure latencies and encapsulate the conversion to seconds without losing precision.
 *
 * <p>
 * Keep in mind that preferred approaches avoid using this mechanism if possible, since latency metrics broken out by
 * outcome should be minimized; {@link Summary#startTimer()} and {@link Histogram#startTimer()} are preferred.
 * Consider moving outcome labels to a separate metric like a counter.
 *
 * <p>
 * Example usage:
 * <pre>
 * {@code
 *   class YourClass {
 *     static final Summary requestLatency = Summary.build()
 *         .name("requests_latency_seconds")
 *         .help("Request latency in seconds.")
 *         .labelNames("aLabel")
 *         .register();
 *
 *     void processRequest(Request req) {
 *        SimpleTimer requestTimer = new SimpleTimer();
 *        try {
 *          // Your code here.
 *        } finally {
 *          requestTimer.labels("aLabelValue").observe(requestTimer.elapsedSeconds());
 *        }
 *     }
 *   }
 * }
 * </pre>
 *
 */
public class SimpleTimer {
  private final long start;
  static TimeProvider defaultTimeProvider = new TimeProvider();
  private final TimeProvider timeProvider;

  static class TimeProvider {
    long nanoTime() {
      return System.nanoTime();
    }
  }

  // Visible for testing.
  SimpleTimer(TimeProvider timeProvider) {
    this.timeProvider = timeProvider;
    start = timeProvider.nanoTime();
  }

  public SimpleTimer() {
    this(defaultTimeProvider);
  }

  /**
   * @return Measured duration in seconds since {@link SimpleTimer} was constructed.
   */
  public double elapsedSeconds() {
    return elapsedSecondsFromNanos(start, timeProvider.nanoTime());
  }

  public static double elapsedSecondsFromNanos(long startNanos, long endNanos) {
      return (endNanos - startNanos) / Collector.NANOSECONDS_PER_SECOND;
  }
}
