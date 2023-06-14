package io.prometheus.client.exporter;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;

public class ExampleExporter {

    static final Gauge g = Gauge.build().name("gauge").help("blah").register();
    static final Counter c = Counter.build().name("counter").help("meh").register();
    static final Summary s = Summary.build().name("summary").help("meh").register();
    static final Histogram h = Histogram.build().name("histogram").help("meh").register();
    static final Gauge l = Gauge.build().name("labels").help("blah").labelNames("l").register();

    public static void main(String[] args) throws Exception {
        new HTTPServer(1234);
        g.set(1);
        c.inc(2);
        s.observe(3);
        h.observe(4);
        l.labels("foo").inc(5);
    }
}
