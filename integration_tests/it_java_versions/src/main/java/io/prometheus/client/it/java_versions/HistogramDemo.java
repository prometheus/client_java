package io.prometheus.client.it.java_versions;

import io.prometheus.metrics.Counter;
import io.prometheus.metrics.Histogram;
import io.prometheus.metrics.SparseHistogram;
import io.prometheus.metrics.Summary;
import io.prometheus.client.exporter.HTTPServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Manual example to test histograms.
 */
public class HistogramDemo {
    public static void main(String[] args) throws IOException, InterruptedException {
        SparseHistogram sh = SparseHistogram.build()
                .name("sparse_example")
                .help("example sparse histogram")
                .labelNames("http_status")
                .withSchema(1)
                .register();
        /*
        Histogram h = Histogram.build()
                .name("response_time")
                .help("example histogram")
                .labelNames("http_status")
                .buckets(0.1, 0.2, 0.3, 0.4)
                .register();
        Summary s = Summary.build()
                .name("summary_example")
                .help("example summary")
                .labelNames("http_status")
                .quantile(0.95, 0.01)
                .quantile(0.99, 0.01)
                .register();
         */
        Counter counter = Counter.build()
                .name("test")
                .help("test counter")
                .labelNames("path")
                .register();
        counter.labels("/hello-world").inc();
        populate(null, sh, null);
        //sh.labels("200").observe(-0.1);
        //sh.labels("200").observe(1024);
        //sh.labels("200").observe(Double.POSITIVE_INFINITY);
        new HTTPServer(9000);
        Thread.currentThread().join(); // sleep forever
    }

    private static void populate(Histogram h, SparseHistogram sh, Summary s) {
        Random rand = new Random(0);
        ArrayList<Double> data = new ArrayList<Double>();
        for (double d = 0.3; d > 0 && d <0.5; d += (rand.nextDouble() - 0.5)/ 10.0) {
            String status = rand.nextBoolean() ? "200" : "500";
            if (status.equals("500")) {
                continue;
            }
            //h.labels(status).observe(d);
            sh.labels(status).observe(d);
            sh.labels(status).observe(-d);
            //sh.labels(status).observe(-d);
            //s.labels(status).observe(d);
            data.add(d);
        }
        Collections.sort(data);
        for (int i=0; i<data.size(); i++) {
            System.out.println((i+1) + ": " + data.get(i));
        }
    }
}
