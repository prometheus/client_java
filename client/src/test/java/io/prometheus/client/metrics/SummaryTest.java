/*
 * Copyright 2013 Prometheus Team Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.prometheus.client.metrics;

import io.prometheus.client.Metrics;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

// TODO(matt): Build fluent matchers to reduce boilerplate.

/**
 * <p>
 * Tests for {@link io.prometheus.client.metrics.Summary}.
 * </p>
 */
public class SummaryTest {
  @Test
  public void workflow() throws InterruptedException {
    Summary.Builder oldBuilder = null;
    Summary.Builder builder = Summary.newBuilder().registerStatic(false);
    Assert.assertNotNull(builder);

    oldBuilder = builder;
    builder = builder.namespace("my_namespace");
    Assert.assertNotEquals("not identical for state transference", builder, oldBuilder);
    oldBuilder = builder;
    builder = builder.subsystem("my_subsystem");
    Assert.assertNotEquals("not identical for state transference", builder, oldBuilder);
    oldBuilder = builder;
    builder = builder.name("my_summary");
    Assert.assertNotEquals("not identical for state transference", builder, oldBuilder);
    oldBuilder = builder;
    builder = builder.documentation("my_documentation");
    Assert.assertNotEquals("not identical for state transference", builder, oldBuilder);
    oldBuilder = builder;
    builder = builder.targetQuantile(0.5, 0.001);
    Assert.assertNotEquals("not identical for state transference", builder, oldBuilder);
    oldBuilder = builder;
    builder = builder.purgeInterval(5, TimeUnit.MINUTES);
    Assert.assertNotEquals("not identical for state transference", builder, oldBuilder);
    oldBuilder = builder;
    builder = builder.labelNames("my_label");
    Assert.assertNotEquals("not identical for state transference", builder, oldBuilder);

    final Summary summary = builder.build();
    Assert.assertNotNull(summary);

    Assert.assertEquals("my_documentation", summary.docstring);
    Assert.assertEquals("my_namespace_my_subsystem_my_summary", summary.name);

    Assert.assertEquals("new metric has no children", 0, summary.children.size());

    final Summary.Partial partial = summary.newPartial();
    Assert.assertNotNull(partial);

    Assert.assertEquals("identical for state transference", partial,
        partial.labelPair("my_label", "my_label_value"));

    final Summary.Child child = partial.apply();
    Assert.assertNotNull(child);

    child.observe(50D);
    Assert.assertEquals("child value is set", 50D, child.query(0.5), 0.001);

    Assert.assertEquals("only one instantiated child", 1, summary.children.size());

    final Summary.Child newChild = partial.apply();
    Assert.assertEquals("partial's identical label signatures should yield the same child", child,
        newChild);

    for (int i = 0; i < 99; i++) {
      newChild.observe(50D);
    }

    for (int i = 0; i < 100; i++) {
      newChild.observe(100D);
    }

    Assert.assertEquals("only one child after mutations", 1, summary.children.size());

    final Metrics.MetricFamily metricFamily = summary.dump();
    Assert.assertNotNull("emitted protocol buffer", metricFamily);

    Assert.assertEquals("correct metric type", Metrics.MetricType.SUMMARY, metricFamily.getType());
    Assert.assertEquals("passed on docstring", "my_documentation", metricFamily.getHelp());
    Assert.assertEquals("passed on name", "my_namespace_my_subsystem_my_summary",
        metricFamily.getName());

    final List<Metrics.Metric> metrics = metricFamily.getMetricList();
    Assert.assertEquals("only one metric child", 1, metrics.size());

    final Metrics.Metric metric = metrics.get(0);
    Assert.assertNotNull(metric);

    final List<Metrics.LabelPair> labels = metric.getLabelList();
    Assert.assertEquals("only one label pair", 1, labels.size());

    final Metrics.LabelPair labelPair = labels.get(0);
    Assert.assertEquals("correct label", "my_label", labelPair.getName());
    Assert.assertEquals("correct value", "my_label_value", labelPair.getValue());

    final Metrics.Summary nestedSummary = metric.getSummary();
    Assert.assertNotNull("set nested type", nestedSummary);

    Assert.assertEquals("observation count", 200, nestedSummary.getSampleCount());
    Assert.assertEquals("observation sum", 15000, nestedSummary.getSampleSum(), 0.001);

    final List<Metrics.Quantile> quantiles = nestedSummary.getQuantileList();
    Assert.assertEquals("quantile count", 1, quantiles.size());

    final Metrics.Quantile quantile = quantiles.get(0);
    Assert.assertEquals("quantile rank", 0.5, quantile.getQuantile(), 0.001);
    Assert.assertEquals("quantile value", 50, quantile.getValue(), 0.001);

    summary.resetAll();

    Assert.assertEquals("same children no. after reset", 1, summary.children.size());

    summary.lastPurgeInstantMs = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1);
    summary.purge();
    Assert.assertEquals("no children after purge", 0, summary.children.size());
  }

  @Test
  public void clonePartialSingle() {
    Summary summary = Summary.newBuilder()
        .name("some-name")
        .documentation("some-documentation")
        .labelNames("a-dimension")
        .registerStatic(false)
        .build();

    Summary.Partial partial = summary.newPartial();
    partial.labelPair("a-dimension", "preset-value");
    Summary.Partial derivative = partial.clone();
    Assert.assertNotSame("should return a new object", partial, derivative);
    Assert.assertSame(partial.apply(), derivative.apply());
    partial.apply().observe(1D);
    derivative.apply().observe(1D);

    Metrics.MetricFamily dump = summary.dump();
    Assert.assertEquals("just one metric", 1, dump.getMetricCount());
  }

  @Test
  public void clonePartialDouble() {
    Summary summary = Summary.newBuilder()
        .name("some-name")
        .documentation("some-documentation")
        .labelNames("a-dimension", "another-dimension")
        .registerStatic(false)
        .build();

    Summary.Partial partial = summary.newPartial();
    partial.labelPair("a-dimension", "preset-value");
    Summary.Partial derivative = partial.clone();
    Assert.assertNotSame("two different partials", partial, derivative);
    partial.labelPair("another-dimension", "first");
    partial.apply().observe(1D);
    derivative.labelPair("another-dimension", "second");
    derivative.apply().observe(1D);

    Metrics.MetricFamily dump = summary.dump();
    Assert.assertEquals("just two metrics", 2, dump.getMetricCount());

    Assert.assertEquals("a-dimension", dump.getMetric(0).getLabel(0).getName());
    Assert.assertEquals("preset-value", dump.getMetric(0).getLabel(0).getValue());
    Assert.assertEquals("another-dimension", dump.getMetric(0).getLabel(1).getName());
    Assert.assertEquals("first", dump.getMetric(0).getLabel(1).getValue());
    Assert.assertEquals(1, dump.getMetric(0).getSummary().getQuantile(0).getValue(), 0);

    Assert.assertEquals("a-dimension", dump.getMetric(1).getLabel(0).getName());
    Assert.assertEquals("preset-value", dump.getMetric(1).getLabel(0).getValue());
    Assert.assertEquals("another-dimension", dump.getMetric(1).getLabel(1).getName());
    Assert.assertEquals("second", dump.getMetric(1).getLabel(1).getValue());
    Assert.assertEquals(1, dump.getMetric(1).getSummary().getQuantile(0).getValue(), 0);
  }

  @Test
  public void emptiedAndSentForExposition() {
    final Summary summary = Summary.newBuilder()
        .name("latencies-by-handler")
        .documentation("some-documentation")
        .labelNames("handler")
        .registerStatic(false)
        .build();

    final Summary.Child fooLatencies = summary.newPartial()
        .labelPair("handler", "foo")
        .apply();

    fooLatencies.observe(5D);

    final Summary.Child barLatencies = summary.newPartial()
        .labelPair("handler", "bar")
        .apply();

    barLatencies.observe(1D);

    // Explicitly clean the slate of one dimension!
    fooLatencies.reset();

    Metrics.MetricFamily out = summary.dump();

    Assert.assertEquals("must contain two metrics", 2, out.getMetricCount());
    final Set<String> found = new HashSet<String>();
    for (final Metrics.Metric m : out.getMetricList()) {
      final String dim = m.getLabel(0).getValue();
      final Metrics.Summary sum = m.getSummary();
      found.add(dim);
      if ("foo".equals(dim)) {
        Assert.assertEquals("should lack ranked values after reset", 0, sum.getQuantileCount());
        Assert.assertEquals("should have record of single observation", 1, sum.getSampleCount());
        Assert.assertEquals("should have sum of single observation", 5, sum.getSampleSum(), 0);
      } else if ("bar".equals(dim)) {
        Assert.assertEquals("should have ranked values", 3, sum.getQuantileCount());
        Assert.assertEquals("should have record of single observation", 1, sum.getSampleCount());
        Assert.assertEquals("should have sum of single observation", 1, sum.getSampleSum(), 0);
      } else {
        Assert.fail("illegal condition");
      }
    }
    Assert.assertTrue("must have seen bar", found.contains("bar"));
    Assert.assertTrue("must have seen foo", found.contains("foo"));
  }

  @Test
  public void resetBehavior() throws InterruptedException {
    final Summary summary = Summary.newBuilder()
        .name("a-metric")
        .documentation("some-documentation")
        .registerStatic(false)
        .labelNames("dimension_a")
        .resetInterval(1, TimeUnit.MILLISECONDS)
        .build();

    summary.newPartial()
        .labelPair("dimension_a", "some_label")
        .apply()
        .observe(100D);

    Thread.sleep(100);  // WART: Synchronization or injected timing obviates.

    summary.reset();
    Metrics.MetricFamily dump = summary.dump();

    Assert.assertEquals(1, dump.getMetricCount());
  }

  @Test
  public void purgeBehavior() throws InterruptedException {
    final Summary summary = Summary.newBuilder()
        .name("a-metric")
        .documentation("some-documentation")
        .registerStatic(false)
        .labelNames("dimension_a")
        .purgeInterval(1, TimeUnit.MILLISECONDS)
        .build();

    summary.newPartial()
        .labelPair("dimension_a", "some_label")
        .apply()
        .observe(100D);

    Thread.sleep(100);  // WART: Synchronization or injected timing obviates.

    summary.purge();
    Metrics.MetricFamily dump = summary.dump();

    Assert.assertEquals(0, dump.getMetricCount());
  }
}
