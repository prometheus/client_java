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

import java.util.List;

/**
 * <p>
 * Tests for {@link Gauge}.
 * </p>
 */
public class GaugeTest {
  @Test
  public void testWorkflow() {
    Gauge.Builder oldBuilder = null;
    Gauge.Builder builder = Gauge.newBuilder().registerStatic(false);
    Assert.assertNotNull(builder);

    oldBuilder = builder;
    builder = builder.namespace("my_namespace");
    Assert.assertNotEquals("not identical for state transference", builder, oldBuilder);
    oldBuilder = builder;
    builder = builder.subsystem("my_subsystem");
    Assert.assertNotEquals("not identical for state transference", builder, oldBuilder);
    oldBuilder = builder;
    builder = builder.name("my_gauge");
    Assert.assertNotEquals("not identical for state transference", builder, oldBuilder);
    oldBuilder = builder;
    builder = builder.documentation("my_documentation");
    Assert.assertNotEquals("not identical for state transference", builder, oldBuilder);
    oldBuilder = builder;
    builder = builder.defaultValue(5D);
    Assert.assertNotEquals("not identical for state transference", builder);
    oldBuilder = builder;
    builder = builder.labelNames("my_label");
    Assert.assertNotEquals("not identical for state transference", builder, oldBuilder);

    final Gauge gauge = builder.build();
    Assert.assertNotNull(gauge);

    Assert.assertEquals("my_documentation", gauge.docstring);
    Assert.assertEquals("my_namespace_my_subsystem_my_gauge", gauge.name);

    Assert.assertEquals("new metric has no children", 0, gauge.children.size());

    final Gauge.Partial partial = gauge.newPartial();
    Assert.assertNotNull(partial);

    Assert.assertEquals("identical for state transference", partial,
        partial.labelPair("my_label", "my_label_value"));

    final Gauge.Child child = partial.apply();
    Assert.assertNotNull(child);

    child.set(42D);
    Assert.assertEquals("child value is set", 42D, child.value.get(), 0.001);

    Assert.assertEquals("only one instantiated child", 1, gauge.children.size());

    final Gauge.Child newChild = partial.apply();
    Assert.assertEquals("partial's identical label signatures should yield the same child", child,
        newChild);

    newChild.set(84D);
    Assert
        .assertEquals("original instantiated child gets new value", 84D, child.value.get(), 0.001);

    Assert.assertEquals("only one child after mutations", 1, gauge.children.size());

    final Metrics.MetricFamily metricFamily = gauge.dump();
    Assert.assertNotNull("emitted protocol buffer", metricFamily);

    Assert.assertEquals("correct metric type", Metrics.MetricType.GAUGE, metricFamily.getType());
    Assert.assertEquals("passed on docstring", "my_documentation", metricFamily.getHelp());
    Assert.assertEquals("passed on name", "my_namespace_my_subsystem_my_gauge",
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

    final Metrics.Gauge nestedGauge = metric.getGauge();
    Assert.assertNotNull("set nested type", nestedGauge);

    Assert.assertEquals("transfered last value", 84, nestedGauge.getValue(), 0.001);

    gauge.resetAll();

    Assert.assertEquals("same children no. after reset", 1, gauge.children.size());
    Assert.assertEquals("got default val. after reset", 5, child.value.get(), 0.001);
  }
}
