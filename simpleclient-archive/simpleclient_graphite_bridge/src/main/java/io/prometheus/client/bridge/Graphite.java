package io.prometheus.client.bridge;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Export metrics in the Graphite plaintext format.
 * <p>
 * <pre>
 * {@code
 *  Graphite g = new Graphite("localhost", 2003);
 *  // Push the default registry once.
 *  g.push(CollectorRegistry.defaultRegistry);
 *
 *  // Push the default registry every 60 seconds.
 *  Thread thread = g.start(CollectorRegistry.defaultRegistry, 60);
 *  // Stop pushing.
 *  thread.interrupt();
 *  thread.join();
 * }
 * </pre>
 * <p>
 */
public class Graphite {
  private static final Logger logger = Logger.getLogger(Graphite.class.getName());

  private final String host;
  private final int port;
  private static final Pattern INVALID_GRAPHITE_CHARS = Pattern.compile("[^a-zA-Z0-9_-]");
  /**
   * Construct a Graphite Bridge with the given host:port.
   */
  public Graphite(String host, int port) {
    this.host = host;
    this.port = port;
  }

  /**
   * Push samples from the given registry to Graphite.
   */
  public void push(CollectorRegistry registry) throws IOException {
    Socket s = new Socket(host, port);
    BufferedWriter writer = new BufferedWriter(new PrintWriter(new OutputStreamWriter(s.getOutputStream(), Charset.forName("UTF-8"))));
    Matcher m = INVALID_GRAPHITE_CHARS.matcher("");
    long now = System.currentTimeMillis() / 1000;
    for (Collector.MetricFamilySamples metricFamilySamples: Collections.list(registry.metricFamilySamples())) {
      for (Collector.MetricFamilySamples.Sample sample: metricFamilySamples.samples) {
        m.reset(sample.name);
        writer.write(m.replaceAll("_"));
        for (int i = 0; i < sample.labelNames.size(); ++i) {
          m.reset(sample.labelValues.get(i));
          writer.write(";" + sample.labelNames.get(i) + "=" + m.replaceAll("_"));
        }
        writer.write(" " + sample.value + " " + now + "\n");
      }
    }
    writer.close();
    s.close();
  }

  /**
   * Push samples from the given registry to Graphite every minute.
   */
  public Thread start(CollectorRegistry registry) {
    return start(registry, 60);
  }

  /**
   * Push samples from the given registry to Graphite at the given interval.
   */
  public Thread start(CollectorRegistry registry, int intervalSeconds) {
    Thread thread = new PushThread(registry, intervalSeconds);
    thread.setDaemon(true);
    thread.start();
    return thread;
  }

  private class PushThread extends Thread {
    private final CollectorRegistry registry;
    private final int intervalSeconds;

    PushThread(CollectorRegistry registry, int intervalSeconds) {
      this.registry = registry;
      this.intervalSeconds = intervalSeconds;
    }

    public void run() {
      long waitUntil = System.currentTimeMillis();
      while (true) {
        try {
          push(registry);
        } catch (IOException e) {
          logger.log(Level.WARNING, "Exception " + e + " pushing to " + host + ":" + port, e);
        }

        long now = System.currentTimeMillis();
        // We may skip some pushes if we're falling behind.
        while (now >= waitUntil) {
          waitUntil += intervalSeconds * 1000;
        }
        try {
          Thread.sleep(waitUntil - now);
        } catch (InterruptedException e) {
          return;
        }
      }
    }
  }
}
