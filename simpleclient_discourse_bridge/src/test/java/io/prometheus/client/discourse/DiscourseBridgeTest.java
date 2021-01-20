package io.prometheus.client.discourse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;

import org.junit.Test;

/**
 * Unit tests for {@code DiscourseBridge}.
 */
public class DiscourseBridgeTest {
  /**
   * Runs the test.
   * @param registry The collector registry.
   * @return The response string.
   * @throws IOException Bubbles up from the write call.
   * @throws InterruptedException Bubbles up from the join call.
   */
  private String runTest(final CollectorRegistry registry) throws IOException, InterruptedException {
    // Server to accept push.
    final ServerSocket ss = new ServerSocket(0);
    final StringBuilder result = new StringBuilder();
    try {
      final Thread t = new Thread() {
        public void run() {
          try {
            final Socket s = ss.accept();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
              result.append(line);
            }
            s.close();
          } catch (final Exception e) {
            e.printStackTrace();
            fail("Unable to read input");
          }
        }
      };
      t.start();

      final DiscourseBridge bridge = new DiscourseBridge("localhost", ss.getLocalPort());

      final StringWriter writer = new StringWriter();
      bridge.getConfig().getFormatter().write(writer, registry.metricFamilySamples());
      System.out.println("\n--- " + new Exception().getStackTrace()[1].getMethodName() + " ---");
      System.out.println(writer.toString());
      System.out.println("---");
      System.out.println(result.toString());
      System.out.println("------");
      System.out.flush();

      bridge.push(registry);
      t.join();
    } finally {
      ss.close();
    }
    return result.toString();
  }

  /**
   * Test creation of {@code DiscourseBridge}.
   */
  @Test
  public void testConstructBridge() {
    final DiscourseBridge bridge = new DiscourseBridge();

    assertNotNull(bridge.getConfig());
    assertEquals("localhost", bridge.getConfig().getHost());
    assertEquals(9394, bridge.getConfig().getPort());
    assertEquals(DiscourseChunkedMetricFamilySamplesTextFormatter.class, bridge.getConfig().getFormatter().getClass());
  }

  /**
   * Test a {@code Counter}.
   * @throws Exception Bubbles up from {@code runTest}.
   */
  @Test
  public void testSimpleCounter() throws Exception {
    final CollectorRegistry registry = new CollectorRegistry();
    final Counter counter = Counter.build()
        .name("my_library_requests_total")
        .help("Total requests.")
        .labelNames("method")
        .register(registry);
    counter.labels("get").inc();
    counter.labels("post").inc();

    assertEquals(1, Collections.list(registry.metricFamilySamples()).size());
    assertEquals(2, registry.metricFamilySamples().nextElement().samples.size());

    runTest(registry);
  }

  /**
   * Test a {@code Histogram}.
   * @throws Exception Bubbles up from {@code runTest}.
   */
  @Test
  public void testSimpleHistogram() throws Exception {
    final CollectorRegistry registry = new CollectorRegistry();
    final Histogram histogram = Histogram.build()
        .name("my_library_requests_latency_seconds")
        .help("Request latency in seconds.")
        .register(registry);
    final Histogram.Timer timer = histogram.startTimer();
    Thread.sleep(50);
    timer.observeDuration();

    assertEquals(1, Collections.list(registry.metricFamilySamples()).size());
    assertEquals(17, registry.metricFamilySamples().nextElement().samples.size());

    runTest(registry);
  }
}
