package io.prometheus.client.bridge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class GraphiteTest {
  @Test
  public void testPush() throws Exception {
    // Create a metric.
    CollectorRegistry registry = new CollectorRegistry();
    Gauge labels = Gauge.build().name("labels").help("help").labelNames("l").register(registry);
    labels.labels("fo*o").inc();


    // Server to accept push.
    final ServerSocket ss = new ServerSocket(0);
    final StringBuilder result = new StringBuilder();
    Thread t = new Thread() {
      public void run() {
        try {
          Socket s = ss.accept();
          BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
          result.append(reader.readLine());
          s.close();
        } catch (Exception e) {
          e.printStackTrace();
          fail();
        }
      }
    };
    t.start();

    // Push.
    Graphite g = new Graphite("localhost", ss.getLocalPort());
    g.push(registry);
    t.join();
    ss.close();

    // Check result.
    String[] parts = result.toString().split(" ");
    assertEquals(3, parts.length);
    assertEquals("labels;l=fo_o", parts[0]);
    assertEquals("1.0", parts[1]);
    Integer.parseInt(parts[2]);  // This shouldn't throw an exception.
  }
}
