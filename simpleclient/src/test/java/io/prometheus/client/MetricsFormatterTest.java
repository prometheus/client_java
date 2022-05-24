package io.prometheus.client;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MetricsFormatterTest {
    CollectorRegistry registry;

    @Before
    public void setup() {
        this.registry = new CollectorRegistry();
    }

    @After
    public void cleanup() {
        this.registry.clear();
    }

    @Test
    public void testGauge() throws IOException {
        Gauge gauge = Gauge.build()
                .name("test_gauge")
                .help("-")
                .labelNames("label1", "label2")
                .register(registry);

        gauge.labels("value1", "value2").inc();

        MetricsFormatter formatter = new MetricsFormatter(NOOP) {
            @Override
            public void format(MetricSnapshotSamples samples) {
                Assert.assertNotNull(samples);
                Assert.assertEquals(samples.name, "test_gauge");
                Assert.assertEquals(samples.help, "-");
                Assert.assertTrue(
                        samples.labelNames.contains("label1") && samples.labelNames.contains("label2"));
                Assert.assertEquals(samples.type, Collector.Type.GAUGE);
                Assert.assertEquals(samples.children.size(), 1);

                for (Map.Entry<List<String>, ?> entry : samples.children) {
                    Object v = entry.getValue();
                    List<String> key = entry.getKey();
                    Assert.assertEquals(key.size(), 2);
                    Assert.assertTrue(key.contains("value1") && key.contains("value2"));
                    Assert.assertTrue(v instanceof Gauge.Child);
                    Gauge.Child child = (Gauge.Child) v;
                    Assert.assertEquals(child.get(), 1.0D, .001);
                }
            }

            @Override
            public void format(List<Collector.MetricFamilySamples> mfs) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean supported(Collector.Type type) {
                return true;
            }
        };

        registry.collect(formatter);
    }


    @Test
    public void testCounter() throws IOException {
        Counter counter = Counter.build()
                .name("test_counter")
                .help("test_counter")
                .labelNames("label1", "label2")
                .register(registry);

        counter.labels("value1", "value2").inc();
        counter.labels("value1", "value2").inc();

        MetricsFormatter formatter = new MetricsFormatter(NOOP) {
            @Override
            public void format(MetricSnapshotSamples samples) {
                Assert.assertNotNull(samples);
                Assert.assertEquals(samples.name, "test_counter");
                Assert.assertEquals(samples.help, "test_counter");
                Assert.assertTrue(
                        samples.labelNames.contains("label1") && samples.labelNames.contains("label2"));
                Assert.assertEquals(samples.type, Collector.Type.COUNTER);
                Assert.assertEquals(samples.children.size(), 1);

                for (Map.Entry<List<String>, ?> entry : samples.children) {
                    Object v = entry.getValue();
                    List<String> key = entry.getKey();
                    Assert.assertEquals(key.size(), 2);
                    Assert.assertTrue(key.contains("value1") && key.contains("value2"));
                    Assert.assertTrue(v instanceof Counter.Child);
                    Counter.Child child = (Counter.Child) v;
                    Assert.assertEquals(child.get(), 2, .001);
                }
            }

            @Override
            public void format(List<Collector.MetricFamilySamples> mfs) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean supported(Collector.Type type) {
                return true;
            }
        };

        registry.collect(formatter);
    }

    @Test
    public void testInfo() throws IOException {
        Info info = Info.build()
                .name("test_info")
                .help("test_info")
                .labelNames("label1", "label2")
                .register(registry);

        info.labels("value1", "value2").info("name", "prometheus", "version", "0.0.1");

        MetricsFormatter formatter = new MetricsFormatter(NOOP) {
            @Override
            public void format(MetricSnapshotSamples samples) {
                Assert.assertNotNull(samples);
                Assert.assertEquals(samples.name, "test_info");
                Assert.assertEquals(samples.help, "test_info");
                Assert.assertTrue(
                        samples.labelNames.contains("label1") && samples.labelNames.contains("label2"));
                Assert.assertEquals(samples.type, Collector.Type.INFO);
                Assert.assertEquals(samples.children.size(), 1);

                for (Map.Entry<List<String>, ?> entry : samples.children) {
                    Object v = entry.getValue();
                    List<String> key = entry.getKey();

                    Assert.assertTrue(key.contains("value1") && key.contains("value2"));
                    Assert.assertTrue(v instanceof Info.Child);

                    Info.Child child = (Info.Child) v;
                    Map<String, String> infos = child.get();
                    Assert.assertEquals(2, infos.size());
                    Assert.assertEquals(infos.get("name"), "prometheus");
                    Assert.assertEquals(infos.get("version"), "0.0.1");
                }
            }

            @Override
            public void format(List<Collector.MetricFamilySamples> mfs) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean supported(Collector.Type type) {
                return true;
            }
        };

        registry.collect(formatter);
    }


    @Test
    public void testHistogram() throws IOException {
        final double[] buckets = new double[]{0.5, 0.9, 0.95, 0.99};

        Histogram histogram = Histogram
                .build("test_histogram", "test_histogram")
                .buckets(buckets)
                .labelNames("label1", "label2")
                .register(registry);

        histogram.labels("value1", "value2").observe(0.1);
        histogram.labels("value1", "value2").observe(0.2);
        histogram.labels("value1", "value2").observe(0.3);
        histogram.labels("value1", "value2").observe(0.6);
        histogram.labels("value1", "value2").observe(0.7);
        histogram.labels("value1", "value2").observe(0.91);


        MetricsFormatter formatter = new MetricsFormatter(NOOP) {
            @Override
            public void format(MetricSnapshotSamples samples) {
                Assert.assertNotNull(samples);
                Assert.assertEquals(samples.name, "test_histogram");
                Assert.assertEquals(samples.help, "test_histogram");
                Assert.assertTrue(
                        samples.labelNames.contains("label1") && samples.labelNames.contains("label2"));
                Assert.assertEquals(samples.type, Collector.Type.HISTOGRAM);
                Assert.assertEquals(samples.children.size(), 1);
                Assert.assertTrue(samples instanceof HistogramSnapshotSamples);
                HistogramSnapshotSamples hsamples = (HistogramSnapshotSamples) samples;
                Assert.assertArrayEquals(hsamples.buckets, new double[]{0.5, 0.9, 0.95, 0.99, Double.POSITIVE_INFINITY}, 0.0);

                for (Map.Entry<List<String>, ?> entry : hsamples.children) {
                    Object v = entry.getValue();
                    List<String> key = entry.getKey();

                    Assert.assertTrue(key.contains("value1") && key.contains("value2"));
                    Assert.assertTrue(v instanceof Histogram.Child);
                    Histogram.Child child = (Histogram.Child) v;
                    Histogram.Child.Value value = child.get();
                    double[] valueBuckets = value.buckets;
                    Assert.assertEquals(value.sum, 0.1 + 0.2 + 0.3 + 0.6 + 0.7 + 0.91, .001);
                    Assert.assertEquals(valueBuckets[0], 3, .001);
                }
            }

            @Override
            public void format(List<Collector.MetricFamilySamples> mfs) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean supported(Collector.Type type) {
                return true;
            }
        };

        registry.collect(formatter);
    }


    @Test
    public void testSummary() throws IOException {
        Summary summary = Summary
                .build("test_summary", "test_summary")
                .quantile(0.5, 0.05)
                .quantile(0.9, 0.01)
                .quantile(0.99, 0.001)
                .labelNames("label1", "label2")
                .register(registry);

        summary.labels("value1", "value2").observe(2);
        summary.labels("value1", "value2").observe(4);


        MetricsFormatter formatter = new MetricsFormatter(NOOP) {
            @Override
            public void format(MetricSnapshotSamples samples) {
                Assert.assertNotNull(samples);
                Assert.assertEquals(samples.name, "test_summary");
                Assert.assertEquals(samples.help, "test_summary");
                Assert.assertTrue(
                        samples.labelNames.contains("label1") && samples.labelNames.contains("label2"));
                Assert.assertEquals(samples.type, Collector.Type.SUMMARY);
                Assert.assertEquals(samples.children.size(), 1);

                for (Map.Entry<List<String>, ?> entry : samples.children) {
                    Object v = entry.getValue();
                    List<String> key = entry.getKey();

                    Assert.assertTrue(key.contains("value1") && key.contains("value2"));
                    Assert.assertTrue(v instanceof Summary.Child);
                    Summary.Child child = (Summary.Child) v;
                    Summary.Child.Value value = child.get();

                    Assert.assertEquals(value.sum, 6, .001);
                    Assert.assertEquals(value.count, 2, .001);
                }
            }

            @Override
            public void format(List<Collector.MetricFamilySamples> mfs) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean supported(Collector.Type type) {
                return true;
            }
        };

        registry.collect(formatter);
    }


    @Test
    public void testEnum() throws IOException {
        Enumeration enumeration = Enumeration
                .build("test_summary", "test_summary")
                .labelNames("label1", "label2")
                .states("a", "b", "c", "d")
                .register(registry);

        enumeration.labels("value1", "value2").state("a");
        enumeration.labels("value1", "value2").state("b");


        MetricsFormatter formatter = new MetricsFormatter(NOOP) {
            @Override
            public void format(MetricSnapshotSamples samples) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void format(List<Collector.MetricFamilySamples> mfs) {
                Assert.assertNotNull(mfs);
            }

            @Override
            public boolean supported(Collector.Type type) {
                return false;
            }
        };

        registry.collect(formatter);
    }


    @Test
    public void testGaugeWithFilter() throws IOException {
        Gauge gauge = Gauge.build()
                .name("test_gauge")
                .help("-")
                .labelNames("label1", "label2")
                .register(registry);

        gauge.labels("value1", "value2").inc();

        MetricsFormatter formatter = new MetricsFormatter(NOOP) {
            @Override
            public void format(MetricSnapshotSamples samples) {
                Assert.assertNotNull(samples);
            }

            @Override
            public void format(List<Collector.MetricFamilySamples> mfs) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean supported(Collector.Type type) {
                return true;
            }
        };

        registry.collect(formatter, new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return s.equals("test_gauge");
            }
        });

        final AtomicBoolean executed = new AtomicBoolean(false);
        MetricsFormatter formatter1 = new MetricsFormatter(NOOP) {
            @Override
            public void format(MetricSnapshotSamples samples) {
                executed.set(true);
            }

            @Override
            public void format(List<Collector.MetricFamilySamples> mfs) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean supported(Collector.Type type) {
                return true;
            }
        };

        registry.collect(formatter1, new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return s.equals("test_gauge_11111");
            }
        });

        Assert.assertFalse(executed.get());
    }

    @Test
    public void testCounterWithFilter() throws IOException {
        Counter counter = Counter.build()
                .name("test_counter")
                .help("-")
                .labelNames("label1", "label2")
                .register(registry);

        counter.labels("value1", "value2").inc();

        MetricsFormatter formatter = new MetricsFormatter(NOOP) {
            @Override
            public void format(MetricSnapshotSamples samples) {
                Assert.assertNotNull(samples);
            }

            @Override
            public void format(List<Collector.MetricFamilySamples> mfs) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean supported(Collector.Type type) {
                return true;
            }
        };

        registry.collect(formatter, new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return s.equals("test_counter");
            }
        });

        final AtomicBoolean executed = new AtomicBoolean(false);
        MetricsFormatter formatter1 = new MetricsFormatter(NOOP) {
            @Override
            public void format(MetricSnapshotSamples samples) {
                executed.set(true);
            }

            @Override
            public void format(List<Collector.MetricFamilySamples> mfs) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean supported(Collector.Type type) {
                return true;
            }
        };

        registry.collect(formatter1, new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return s.equals("test_counter_1111");
            }
        });

        Assert.assertFalse(executed.get());
    }


    private static final MetricsFormatter.MetricsWriter NOOP = new NoopMetricsWriter();

    private static class NoopMetricsWriter extends MetricsFormatter.MetricsWriter {

        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException {
            //noop
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            //noop
        }

        @Override
        public void flush() throws IOException {
            //noop
        }

        @Override
        public void close() throws IOException {
            //noop
        }
    }
}
