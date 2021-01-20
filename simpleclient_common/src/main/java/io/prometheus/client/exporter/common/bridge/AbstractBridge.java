package io.prometheus.client.exporter.common.bridge;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract implementation of {@code Bridge}.
 */
public abstract class AbstractBridge<C extends BridgeConfig> implements Bridge {
  /**
   * The logging utility.
   */
  protected static final Logger LOGGER = Logger.getLogger(AbstractBridge.class.getName());

  /**
   * The bridge configuration.
   */
  private final C config;

  /**
   * Construct a bridge with the specified configuration.
   * @param config The bridge configuration.
   */
  protected AbstractBridge(final C config) {
    this.config = config;
  }

  /**
   * Returns the bridge configuration.
   * @return The config.
   */
  public C getConfig() {
    return this.config;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void push(final CollectorRegistry registry) throws IOException {
    final Socket s = new Socket(getConfig().getHost(), getConfig().getPort());
    final BufferedWriter writer = new BufferedWriter(new PrintWriter(new OutputStreamWriter(s.getOutputStream(), getConfig().getFormatter().getCharset())));
    try {
      getConfig().getFormatter().write(writer, registry.metricFamilySamples());
    } finally {
      writer.close();
      s.close();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Thread start(final CollectorRegistry registry, final int intervalSeconds) {
    final Thread thread = new PushThread(registry, intervalSeconds);
    thread.setDaemon(true);
    thread.start();
    return thread;
  }

  /**
   * Push samples from the given registry to external process every minute.
   * @param registry The collector registry.
   * @return The started tread.
   */
  public Thread start(final CollectorRegistry registry) {
    return start(registry, 60);
  }

  /**
   * The thread which pushes the metrics.
   */
  private class PushThread extends Thread {
    /**
     * The collection registry.
     */
    private final CollectorRegistry registry;

    /**
     * The interval in seconds.
     */
    private final int intervalSeconds;

    /**
     * Constructs a new instance of {@code PushThread}.
     * @param registry The collection registry.
     * @param intervalSeconds The interval in seconds to publish.
     */
    PushThread(CollectorRegistry registry, int intervalSeconds) {
      this.registry = registry;
      this.intervalSeconds = intervalSeconds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
      long waitUntil = System.currentTimeMillis();
      while (true) {
        try {
          push(registry);
        } catch (final IOException e) {
          LOGGER.log(Level.WARNING, "Exception " + e + " pushing to " + getConfig().getHost() + ":" + getConfig().getPort(), e);
        }

        final long now = System.currentTimeMillis();
        // We may skip some pushes if we're falling behind.
        while (now >= waitUntil) {
          waitUntil += intervalSeconds * 1000;
        }
        try {
          Thread.sleep(waitUntil - now);
        } catch (final InterruptedException e) {
          return;
        }
      }
    }
  }
}
