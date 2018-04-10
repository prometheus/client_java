package io.prometheus.client.spectator;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.netflix.spectator.api.Counter;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.DistributionSummary;
import com.netflix.spectator.api.Gauge;
import com.netflix.spectator.api.Spectator;
import com.netflix.spectator.api.Timer;

import io.prometheus.client.CollectorRegistry;

public class SpectatorExportsTest {

  @Test
  public void test() {
    CollectorRegistry registry = new CollectorRegistry();

    DefaultRegistry spectatorRegistry = new DefaultRegistry();
    Spectator.globalRegistry().add(spectatorRegistry);

    new SpectatorExports(Spectator.globalRegistry()).register(registry);

    Counter counter = spectatorRegistry.counter("testCounter", "k1", "v1");
    counter.increment();

    Gauge gauge = spectatorRegistry.gauge("testGauge", "k2", "v2");
    gauge.set(99.99);

    Timer timer = spectatorRegistry.timer("testTimer", "k3", "v3");
    timer.record(666, TimeUnit.NANOSECONDS);

    DistributionSummary distributionSummary = spectatorRegistry
        .distributionSummary("testDistributionSummary", "k4", "v4");
    distributionSummary.record(88);

    assertEquals(1, registry.getSampleValue("testCounter", new String[] {"k1"}, new String[] {"v1"}), 0);

    assertEquals(1, registry.getSampleValue("testCounter", new String[] {"k1"}, new String[] {"v1"}), 0);
    assertEquals(99.99, registry.getSampleValue("testGauge", new String[] {"k2"}, new String[] {"v2"}), 0);
    assertEquals(1,
        registry.getSampleValue("testTimer", new String[] {"k3", "statistic"}, new String[] {"v3", "count"}), 0);
    assertEquals(666,
        registry.getSampleValue("testTimer", new String[] {"k3", "statistic"}, new String[] {"v3", "totalTime"}), 0);

    assertEquals(1, registry
        .getSampleValue("testDistributionSummary", new String[] {"k4", "statistic"}, new String[] {"v4", "count"}), 0);
    assertEquals(88, registry.getSampleValue("testDistributionSummary", new String[] {"k4", "statistic"},
        new String[] {"v4", "totalAmount"}), 0);
  }
}