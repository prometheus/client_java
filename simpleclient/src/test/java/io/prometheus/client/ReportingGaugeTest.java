package io.prometheus.client;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class ReportingGaugeTest {

  CollectorRegistry registry;
  ReportingGauge noLabels, labels;

  @Before
  public void setUp() {
    registry = new CollectorRegistry();
    noLabels = new ReportingGauge("nolabels","help", new ArrayList<String>()).register(registry);
    labels = new ReportingGauge("labels", "help", Arrays.asList("l")).register(registry);
  }

  @After
  public void tearDown() {
    Gauge.Child.timeProvider = new Gauge.TimeProvider();
  }

  @Test
  public void testCollect() {
    labels.add(new ReportingGauge.Reporter() {
      @Override
      public ReportingGauge.Sample get() {
        return ReportingGauge.Sample.withLabels("a").value(1.0);
      }
    });
    List<Collector.MetricFamilySamples> mfs = labels.collect();
    
    ArrayList<Collector.MetricFamilySamples.Sample> samples = new ArrayList<Collector.MetricFamilySamples.Sample>();
    ArrayList<String> labelNames = new ArrayList<String>();
    labelNames.add("l");
    ArrayList<String> labelValues = new ArrayList<String>();
    labelValues.add("a");
    samples.add(new Collector.MetricFamilySamples.Sample("labels", labelNames, labelValues, 1.0));
    Collector.MetricFamilySamples mfsFixture = new Collector.MetricFamilySamples("labels", Collector.Type.GAUGE, "help", samples);

    assertEquals(1, mfs.size());
    assertEquals(mfsFixture, mfs.get(0));
  }

}
