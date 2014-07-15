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
 * Tests for {@link Counter}.
 * </p>
 */
public class CounterTest {
  @Test
  public void workflow() {
    Counter.Builder oldBuilder = null;
    Counter.Builder builder = Counter.newBuilder().registerStatic(false);
    Assert.assertNotNull(builder);

    oldBuilder = builder;
    builder = builder.namespace("my_namespace");
    Assert.assertNotEquals("not identical for state transference", builder, oldBuilder);
    oldBuilder = builder;
    builder = builder.subsystem("my_subsystem");
    Assert.assertNotEquals("not identical for state transference", builder, oldBuilder);
    oldBuilder = builder;
    builder = builder.name("my_counter");
    Assert.assertNotEquals("not identical for state transference", builder, oldBuilder);
    oldBuilder = builder;
    builder = builder.documentation("my_documentation");
    Assert.assertNotEquals("not identical for state transference", builder, oldBuilder);
    oldBuilder = builder;
    builder = builder.defaultValue(13D);
    Assert.assertNotEquals("not identical for state transference", builder, oldBuilder);
    oldBuilder = builder;
    builder = builder.labelNames("my_label");
    Assert.assertNotEquals("not identical for state transference", builder, oldBuilder);

    final Counter Counter = builder.build();
    Assert.assertNotNull(Counter);

    Assert.assertEquals("my_documentation", Counter.docstring);
    Assert.assertEquals("my_namespace_my_subsystem_my_counter", Counter.name);

    Assert.assertEquals("new metric has no children", 0, Counter.children.size());

    final Counter.Partial partial = Counter.newPartial();
    Assert.assertNotNull(partial);

    Assert.assertEquals("identical for state transference", partial,
        partial.labelPair("my_label", "my_label_value"));

    final Counter.Child child = partial.apply();
    Assert.assertNotNull(child);

    child.set(42D);
    Assert.assertEquals("child value is set", 42D, child.value.get(), 0.001);

    Assert.assertEquals("only one instantiated child", 1, Counter.children.size());

    final Counter.Child newChild = partial.apply();
    Assert.assertEquals("partial's identical label signatures should yield the same child", child,
        newChild);

    newChild.increment();
    Assert.assertEquals("original instantiated child incremented", 43, child.value.get(), 0.001);
    newChild.increment(5);
    Assert.assertEquals("original instantiated child incremented by five", 48, child.value.get(),
        0.001);

    Assert.assertEquals("only one child after mutations", 1, Counter.children.size());

    final Metrics.MetricFamily metricFamily = Counter.dump();
    Assert.assertNotNull("emitted protocol buffer", metricFamily);

    Assert.assertEquals("correct metric type", Metrics.MetricType.COUNTER, metricFamily.getType());
    Assert.assertEquals("passed on docstring", "my_documentation", metricFamily.getHelp());
    Assert.assertEquals("passed on name", "my_namespace_my_subsystem_my_counter",
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

    final Metrics.Counter nestedCounter = metric.getCounter();
    Assert.assertNotNull("set nested type", nestedCounter);

    Assert.assertEquals("transferred last value", 48, nestedCounter.getValue(), 0.001);

    Counter.resetAll();

    Assert.assertEquals("same children no. after reset", 1, Counter.children.size());
    Assert.assertEquals("got default val. after reset", 13, child.value.get(), 0.001);
  }

  @Test
  public void clonePartialSingle() {
    Counter counter = Counter.newBuilder()
        .name("some-name")
        .documentation("some-documentation")
        .labelNames("a-dimension")
        .registerStatic(false)
        .build();

    Counter.Partial partial = counter.newPartial();
    partial.labelPair("a-dimension", "preset-value");
    Counter.Partial derivative = partial.clone();
    Assert.assertNotSame("should return a new object", partial, derivative);
    Assert.assertSame(partial.apply(), derivative.apply());
    partial.apply().increment();
    derivative.apply().increment();

    Metrics.MetricFamily dump = counter.dump();
    Assert.assertEquals("just one metric", 1, dump.getMetricCount());
  }

  @Test
  public void clonePartialDouble() {
    Counter counter = Counter.newBuilder()
        .name("some-name")
        .documentation("some-documentation")
        .labelNames("a-dimension", "another-dimension")
        .registerStatic(false)
        .build();

    Counter.Partial partial = counter.newPartial();
    partial.labelPair("a-dimension", "preset-value");
    Counter.Partial derivative = partial.clone();
    Assert.assertNotSame("two different partials", partial, derivative);
    partial.labelPair("another-dimension", "first");
    partial.apply().increment();
    derivative.labelPair("another-dimension", "second");
    derivative.apply().increment();

    Metrics.MetricFamily dump = counter.dump();
    Assert.assertEquals("just two metrics", 2, dump.getMetricCount());

    Assert.assertEquals("a-dimension", dump.getMetric(0).getLabel(0).getName());
    Assert.assertEquals("preset-value", dump.getMetric(0).getLabel(0).getValue());
    Assert.assertEquals("another-dimension", dump.getMetric(0).getLabel(1).getName());
    Assert.assertEquals("first", dump.getMetric(0).getLabel(1).getValue());
    Assert.assertEquals(1, dump.getMetric(0).getCounter().getValue(), 0);

    Assert.assertEquals("a-dimension", dump.getMetric(1).getLabel(0).getName());
    Assert.assertEquals("preset-value", dump.getMetric(1).getLabel(0).getValue());
    Assert.assertEquals("another-dimension", dump.getMetric(1).getLabel(1).getName());
    Assert.assertEquals("second", dump.getMetric(1).getLabel(1).getValue());
    Assert.assertEquals(1, dump.getMetric(1).getCounter().getValue(), 0);
  }
}
