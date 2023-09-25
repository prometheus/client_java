package io.prometheus.client.it.pushgateway;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.BasicAuthHttpConnectionFactory;
import io.prometheus.client.exporter.PushGateway;

public class ExampleBatchJob {

    // The following is copy-and-paste from README.md
    // except that we added basic authentication to test the BasicAuthHttpConnectionFactory with different Java versions.
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("Usage: batch-job.jar <address> <user> <password>");
            System.exit(-1);
        }
        CollectorRegistry registry = new CollectorRegistry();
        Gauge duration = Gauge.build()
                .name("my_batch_job_duration_seconds")
                .help("Duration of my batch job in seconds.")
                .register(registry);
        Gauge.Timer durationTimer = duration.startTimer();
        try {
            Gauge lastSuccess = Gauge.build()
                    .name("my_batch_job_last_success")
                    .help("Last time my batch job succeeded, in unixtime.")
                    .register(registry);
            lastSuccess.setToCurrentTime();
        } finally {
            durationTimer.setDuration();
            PushGateway pg = new PushGateway(args[0]);
            pg.setConnectionFactory(new BasicAuthHttpConnectionFactory(args[1], args[2]));
            pg.pushAdd(registry, "my_batch_job");
        }
    }
}
