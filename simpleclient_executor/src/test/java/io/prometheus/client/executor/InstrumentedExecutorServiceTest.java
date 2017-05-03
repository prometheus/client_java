package io.prometheus.client.executor;

import static org.junit.Assert.assertEquals;

import io.prometheus.client.CollectorRegistry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.Test;

public class InstrumentedExecutorServiceTest {

  private static final String NAME = "exs";

  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  private final InstrumentedExecutorService instrumentedExecutorService = new InstrumentedExecutorService(
      executor,
      NAME);

  private final CollectorRegistry registry = CollectorRegistry.defaultRegistry;

  private final String[] labelNames = {"executor"};
  private final String[] labelValues = {NAME};

  @Test
  public void testSubmittingProcess() throws Exception {
    assertEquals(new Double(0),
        registry.getSampleValue("service_submitted_total", labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("service_running", labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("service_completed_total", labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("service_duration_seconds_count", labelNames,
            labelValues));

    Future<?> future = instrumentedExecutorService.submit(new Runnable() {
      public void run() {
        assertEquals(new Double(1),
            registry.getSampleValue("service_submitted_total", labelNames,
                labelValues));
        assertEquals(new Double(1),
            registry.getSampleValue("service_running", labelNames,
                labelValues));
        assertEquals(new Double(0),
            registry.getSampleValue("service_completed_total", labelNames,
                labelValues));
        assertEquals(new Double(0),
            registry
                .getSampleValue("service_duration_seconds_count", labelNames,
                    labelValues));
      }
    });

    future.get();
    assertEquals(new Double(1),
        registry.getSampleValue("service_submitted_total", labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("service_running", labelNames,
            labelValues));
    assertEquals(new Double(1),
        registry.getSampleValue("service_completed_total", labelNames,
            labelValues));
    assertEquals(new Double(1),
        registry.getSampleValue("service_duration_seconds_count", labelNames,
            labelValues));
  }
}
