package io.prometheus.client.executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import io.prometheus.client.CollectorRegistry;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class InstrumentedScheduledExecutorServiceTest {

  private final ScheduledExecutorService scheduledExecutor = Executors
      .newSingleThreadScheduledExecutor();

  private final CollectorRegistry registry = CollectorRegistry.defaultRegistry;

  private final String[] labelNames = {"executor"};

  @Test
  public void testSubmitRunnable() throws Exception {
    final String name = "exs1";
    final String[] labelValues = {name};
    InstrumentedScheduledExecutorService instrumentedScheduledExecutor =
        new InstrumentedScheduledExecutorService(scheduledExecutor, name);

    assertEquals(new Double(0),
        registry
            .getSampleValue("scheduled_service_submitted_total", labelNames,
                labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_running", labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry
            .getSampleValue("scheduled_service_completed_total", labelNames,
                labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_duration_seconds_count",
            labelNames,
            labelValues));

    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_scheduled_once_total",
            labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry
            .getSampleValue("scheduled_service_scheduled_repetitively_total",
                labelNames,
                labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_scheduled_overrun_total",
            labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_period_percentage_count",
            labelNames,
            labelValues));

    Future<?> future = instrumentedScheduledExecutor.submit(new Runnable() {
      public void run() {
        assertEquals(new Double(1),
            registry.getSampleValue("scheduled_service_submitted_total",
                labelNames,
                labelValues));
        assertEquals(new Double(1),
            registry
                .getSampleValue("scheduled_service_running", labelNames,
                    labelValues));
        assertEquals(new Double(0),
            registry.getSampleValue("scheduled_service_completed_total",
                labelNames,
                labelValues));
        assertEquals(new Double(0),
            registry
                .getSampleValue("scheduled_service_duration_seconds_count",
                    labelNames,
                    labelValues));

        assertEquals(new Double(0),
            registry.getSampleValue("scheduled_service_scheduled_once_total",
                labelNames,
                labelValues));
        assertEquals(new Double(0),
            registry.getSampleValue(
                "scheduled_service_scheduled_repetitively_total", labelNames,
                labelValues));
        assertEquals(new Double(0),
            registry
                .getSampleValue("scheduled_service_scheduled_overrun_total",
                    labelNames,
                    labelValues));
        assertEquals(new Double(0),
            registry
                .getSampleValue("scheduled_service_period_percentage_count",
                    labelNames,
                    labelValues));
      }
    });

    future.get();
    assertEquals(new Double(1),
        registry
            .getSampleValue("scheduled_service_submitted_total", labelNames,
                labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_running", labelNames,
            labelValues));
    assertEquals(new Double(1),
        registry
            .getSampleValue("scheduled_service_completed_total", labelNames,
                labelValues));
    assertEquals(new Double(1),
        registry.getSampleValue("scheduled_service_duration_seconds_count",
            labelNames,
            labelValues));

    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_scheduled_once_total",
            labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry
            .getSampleValue("scheduled_service_scheduled_repetitively_total",
                labelNames,
                labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_scheduled_overrun_total",
            labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_period_percentage_count",
            labelNames,
            labelValues));
  }

  @Test
  public void testScheduleRunnable() throws Exception {
    final String name = "exs2";
    final String[] labelValues = {name};
    InstrumentedScheduledExecutorService instrumentedScheduledExecutor =
        new InstrumentedScheduledExecutorService(scheduledExecutor, name);

    assertEquals(new Double(0),
        registry
            .getSampleValue("scheduled_service_submitted_total", labelNames,
                labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_running", labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry
            .getSampleValue("scheduled_service_completed_total", labelNames,
                labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_duration_seconds_count",
            labelNames,
            labelValues));

    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_scheduled_once_total",
            labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry
            .getSampleValue("scheduled_service_scheduled_repetitively_total",
                labelNames,
                labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_scheduled_overrun_total",
            labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_period_percentage_count",
            labelNames,
            labelValues));

    ScheduledFuture<?> theFuture = instrumentedScheduledExecutor.schedule(new Runnable() {
      public void run() {
        assertEquals(new Double(0),
            registry
                .getSampleValue("scheduled_service_submitted_total",
                    labelNames,
                    labelValues));
        assertEquals(new Double(1),
            registry
                .getSampleValue("scheduled_service_running", labelNames,
                    labelValues));
        assertEquals(new Double(0),
            registry
                .getSampleValue("scheduled_service_completed_total",
                    labelNames,
                    labelValues));
        assertEquals(new Double(0),
            registry
                .getSampleValue("scheduled_service_duration_seconds_count",
                    labelNames,
                    labelValues));

        assertEquals(new Double(1),
            registry.getSampleValue("scheduled_service_scheduled_once_total",
                labelNames,
                labelValues));
        assertEquals(new Double(0),
            registry
                .getSampleValue(
                    "scheduled_service_scheduled_repetitively_total",
                    labelNames,
                    labelValues));
        assertEquals(new Double(0),
            registry
                .getSampleValue("scheduled_service_scheduled_overrun_total",
                    labelNames,
                    labelValues));
        assertEquals(new Double(0),
            registry
                .getSampleValue("scheduled_service_period_percentage_count",
                    labelNames,
                    labelValues));
      }
    }, 10L, TimeUnit.MILLISECONDS);

    theFuture.get();

    assertEquals(new Double(0),
        registry
            .getSampleValue("scheduled_service_submitted_total", labelNames,
                labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_running", labelNames,
            labelValues));
    assertEquals(new Double(1),
        registry
            .getSampleValue("scheduled_service_completed_total", labelNames,
                labelValues));
    assertEquals(new Double(1),
        registry.getSampleValue("scheduled_service_duration_seconds_count",
            labelNames,
            labelValues));

    assertEquals(new Double(1),
        registry.getSampleValue("scheduled_service_scheduled_once_total",
            labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry
            .getSampleValue("scheduled_service_scheduled_repetitively_total",
                labelNames,
                labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_scheduled_overrun_total",
            labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_period_percentage_count",
            labelNames,
            labelValues));
  }

  @Test
  public void testSubmitCallable() throws Exception {
    final String name = "exs3";
    final String[] labelValues = {name};
    InstrumentedScheduledExecutorService instrumentedScheduledExecutor =
        new InstrumentedScheduledExecutorService(scheduledExecutor, name);

    assertEquals(new Double(0),
        registry
            .getSampleValue("scheduled_service_submitted_total", labelNames,
                labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_running", labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry
            .getSampleValue("scheduled_service_completed_total", labelNames,
                labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_duration_seconds_count",
            labelNames,
            labelValues));

    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_scheduled_once_total",
            labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry
            .getSampleValue("scheduled_service_scheduled_repetitively_total",
                labelNames,
                labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_scheduled_overrun_total",
            labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_period_percentage_count",
            labelNames,
            labelValues));

    final Object obj = new Object();

    Future<Object> theFuture = instrumentedScheduledExecutor.submit(new Callable<Object>() {
      public Object call() {
        assertEquals(new Double(1),
            registry
                .getSampleValue("scheduled_service_submitted_total",
                    labelNames,
                    labelValues));
        assertEquals(new Double(1),
            registry
                .getSampleValue("scheduled_service_running", labelNames,
                    labelValues));
        assertEquals(new Double(0),
            registry
                .getSampleValue("scheduled_service_completed_total",
                    labelNames,
                    labelValues));
        assertEquals(new Double(0),
            registry
                .getSampleValue("scheduled_service_duration_seconds_count",
                    labelNames,
                    labelValues));

        assertEquals(new Double(0),
            registry.getSampleValue("scheduled_service_scheduled_once_total",
                labelNames,
                labelValues));
        assertEquals(new Double(0),
            registry
                .getSampleValue(
                    "scheduled_service_scheduled_repetitively_total",
                    labelNames,
                    labelValues));
        assertEquals(new Double(0),
            registry
                .getSampleValue("scheduled_service_scheduled_overrun_total",
                    labelNames,
                    labelValues));
        assertEquals(new Double(0),
            registry
                .getSampleValue("scheduled_service_period_percentage_count",
                    labelNames,
                    labelValues));
        return obj;
      }
    });

    assertEquals(obj, theFuture.get());

    assertEquals(new Double(1),
        registry
            .getSampleValue("scheduled_service_submitted_total", labelNames,
                labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_running", labelNames,
            labelValues));
    assertEquals(new Double(1),
        registry
            .getSampleValue("scheduled_service_completed_total", labelNames,
                labelValues));
    assertEquals(new Double(1),
        registry.getSampleValue("scheduled_service_duration_seconds_count",
            labelNames,
            labelValues));

    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_scheduled_once_total",
            labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry
            .getSampleValue("scheduled_service_scheduled_repetitively_total",
                labelNames,
                labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_scheduled_overrun_total",
            labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_period_percentage_count",
            labelNames,
            labelValues));
  }

  @Test
  public void testScheduleCallable() throws Exception {
    final String name = "exs4";
    final String[] labelValues = {name};
    InstrumentedScheduledExecutorService instrumentedScheduledExecutor =
        new InstrumentedScheduledExecutorService(scheduledExecutor, name);

    assertEquals(new Double(0),
        registry
            .getSampleValue("scheduled_service_submitted_total", labelNames,
                labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_running", labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry
            .getSampleValue("scheduled_service_completed_total", labelNames,
                labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_duration_seconds_count",
            labelNames,
            labelValues));

    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_scheduled_once_total",
            labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry
            .getSampleValue("scheduled_service_scheduled_repetitively_total",
                labelNames,
                labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_scheduled_overrun_total",
            labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_period_percentage_count",
            labelNames,
            labelValues));

    final Object obj = new Object();

    ScheduledFuture<Object> theFuture = instrumentedScheduledExecutor
        .schedule(new Callable<Object>() {
          public Object call() {
            assertEquals(new Double(0),
                registry
                    .getSampleValue("scheduled_service_submitted_total",
                        labelNames,
                        labelValues));
            assertEquals(new Double(1),
                registry.getSampleValue("scheduled_service_running",
                    labelNames,
                    labelValues));
            assertEquals(new Double(0),
                registry
                    .getSampleValue("scheduled_service_completed_total",
                        labelNames,
                        labelValues));
            assertEquals(new Double(0),
                registry.getSampleValue(
                    "scheduled_service_duration_seconds_count",
                    labelNames,
                    labelValues));

            assertEquals(new Double(1),
                registry
                    .getSampleValue("scheduled_service_scheduled_once_total",
                        labelNames,
                        labelValues));
            assertEquals(new Double(0),
                registry
                    .getSampleValue(
                        "scheduled_service_scheduled_repetitively_total",
                        labelNames,
                        labelValues));
            assertEquals(new Double(0),
                registry.getSampleValue(
                    "scheduled_service_scheduled_overrun_total",
                    labelNames,
                    labelValues));
            assertEquals(new Double(0),
                registry.getSampleValue(
                    "scheduled_service_period_percentage_count",
                    labelNames,
                    labelValues));
            return obj;
          }
        }, 10L, TimeUnit.MILLISECONDS);

    assertEquals(obj, theFuture.get());

    assertEquals(new Double(0),
        registry
            .getSampleValue("scheduled_service_submitted_total", labelNames,
                labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_running", labelNames,
            labelValues));
    assertEquals(new Double(1),
        registry
            .getSampleValue("scheduled_service_completed_total", labelNames,
                labelValues));
    assertEquals(new Double(1),
        registry.getSampleValue("scheduled_service_duration_seconds_count",
            labelNames,
            labelValues));

    assertEquals(new Double(1),
        registry.getSampleValue("scheduled_service_scheduled_once_total",
            labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry
            .getSampleValue("scheduled_service_scheduled_repetitively_total",
                labelNames,
                labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_scheduled_overrun_total",
            labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_period_percentage_count",
            labelNames,
            labelValues));
  }

  @Test
  public void testScheduleFixedRateCallable() throws Exception {
    final String name = "exs5";
    final String[] labelValues = {name};
    InstrumentedScheduledExecutorService instrumentedScheduledExecutor =
        new InstrumentedScheduledExecutorService(scheduledExecutor, name);

    assertEquals(new Double(0),
        registry
            .getSampleValue("scheduled_service_submitted_total", labelNames,
                labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_running", labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry
            .getSampleValue("scheduled_service_completed_total", labelNames,
                labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_duration_seconds_count",
            labelNames,
            labelValues));

    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_scheduled_once_total",
            labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry
            .getSampleValue("scheduled_service_scheduled_repetitively_total",
                labelNames,
                labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_scheduled_overrun_total",
            labelNames,
            labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_period_percentage_count",
            labelNames,
            labelValues));
    ScheduledFuture<?> theFuture = instrumentedScheduledExecutor
        .scheduleAtFixedRate(new Runnable() {
          public void run() {
            assertEquals(new Double(0),
                registry
                    .getSampleValue("scheduled_service_submitted_total",
                        labelNames,
                        labelValues));
            assertEquals(new Double(1),
                registry.getSampleValue("scheduled_service_running",
                    labelNames,
                    labelValues));
            assertEquals(new Double(0),
                registry
                    .getSampleValue("scheduled_service_completed_total",
                        labelNames,
                        labelValues));
            assertEquals(new Double(0),
                registry.getSampleValue(
                    "scheduled_service_duration_seconds_count",
                    labelNames,
                    labelValues));

            assertEquals(new Double(0),
                registry
                    .getSampleValue("scheduled_service_scheduled_once_total",
                        labelNames,
                        labelValues));
            assertEquals(new Double(1),
                registry
                    .getSampleValue(
                        "scheduled_service_scheduled_repetitively_total",
                        labelNames,
                        labelValues));
            assertEquals(new Double(0),
                registry.getSampleValue(
                    "scheduled_service_scheduled_overrun_total",
                    labelNames,
                    labelValues));
            assertEquals(new Double(0),
                registry.getSampleValue(
                    "scheduled_service_period_percentage_count",
                    labelNames,
                    labelValues));
            try {
              TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException ex) {
              Thread.currentThread().interrupt();
            }

            return;
          }
        }, 10L, 10L, TimeUnit.MILLISECONDS);

    TimeUnit.MILLISECONDS.sleep(100);
    theFuture.cancel(true);
    TimeUnit.MILLISECONDS.sleep(100);

    assertEquals(new Double(0),
        registry
            .getSampleValue("scheduled_service_submitted_total", labelNames,
                labelValues));
    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_running", labelNames,
            labelValues));
    assertNotEquals(new Double(0),
        registry
            .getSampleValue("scheduled_service_completed_total", labelNames,
                labelValues));
    assertNotEquals(new Double(0),
        registry.getSampleValue("scheduled_service_duration_seconds_count",
            labelNames,
            labelValues));

    assertEquals(new Double(0),
        registry.getSampleValue("scheduled_service_scheduled_once_total",
            labelNames,
            labelValues));
    assertEquals(new Double(1),
        registry
            .getSampleValue("scheduled_service_scheduled_repetitively_total",
                labelNames,
                labelValues));
    assertNotEquals(new Double(0),
        registry.getSampleValue("scheduled_service_scheduled_overrun_total",
            labelNames,
            labelValues));
    assertNotEquals(new Double(0),
        registry.getSampleValue("scheduled_service_period_percentage_count",
            labelNames,
            labelValues));
  }
}
