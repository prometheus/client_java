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

import com.google.common.collect.Lists;
import io.prometheus.client.Metrics;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * <p>
 * Tests for {@link io.prometheus.client.metrics.Metric}.
 * </p>
 */
public class MetricTest {
  @Test(expected = Metric.IllegalLabelDeclarationException.class)
  public void emptyLabelIllegality() {
    Metric.BaseBuilder builder = new Metric.BaseBuilder();
    builder = builder.labelNames("");
    builder.buildLabelNames();
  }

  @Test(expected = Metric.IllegalLabelDeclarationException.class)
  public void reservedLabelIllegality() {
    Metric.BaseBuilder builder = new Metric.BaseBuilder();
    builder = builder.labelNames("__name");
    builder.buildLabelNames();
  }

  @Test
  public void allowedLabelName() {
    Metric.BaseBuilder builder = new Metric.BaseBuilder();
    builder = builder.labelNames("name");
    Assert.assertEquals(Lists.newArrayList("name"), builder.buildLabelNames());
  }
}
