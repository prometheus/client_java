package io.prometheus.client.examples.guice.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.prometheus.client.metrics.Counter;
import io.prometheus.client.metrics.Summary;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class BarServlet extends HttpServlet {
  private final Counter.Partial requestPartial;
  private final Summary.Partial latencyPartial;

  @Inject
  public BarServlet(final @Named("handler") Counter requests,
      final @Named("handler") Summary latencies) {

    requestPartial = requests.newPartial().labelPair("handler", "bar");
    latencyPartial = latencies.newPartial().labelPair("handler", "bar");
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {
    final long start = System.currentTimeMillis();

    final Counter.Partial count = requestPartial.clone();
    final Summary.Partial latency = latencyPartial.clone();
    try {
      // Fictitiously take up to 100ms to provision a response.
      Thread.sleep((long)(Math.random() * 100));

      resp.setStatus(HttpServletResponse.SC_OK);
      resp.getWriter().write("Here's one bar for you!");

      count.labelPair("result", "success");
      latency.labelPair("result", "success");
    } catch (final IOException e) {
      count.labelPair("result", "failure");
      latency.labelPair("result", "failure");
      throw e;
    } catch (final RuntimeException e) {
      count.labelPair("result", "failure");
      latency.labelPair("result", "failure");
      throw e;
    } catch (final InterruptedException e) {
      count.labelPair("result", "failure");
      latency.labelPair("result", "failure");
    } finally {
      final long dur = System.currentTimeMillis() - start;
      count.apply().increment();
      latency.apply().observe((double)dur);
    }
  }
}
