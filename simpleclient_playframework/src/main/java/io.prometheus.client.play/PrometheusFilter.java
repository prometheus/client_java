package io.prometheus.client.play;

import akka.stream.Materializer;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import play.mvc.Filter;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class PrometheusFilter extends Filter {

    private static Counter requestsTotal = Counter.build()
            .name("http_requests_total").help("Number of requests")
            .labelNames("method", "status_code").register();

    private static Histogram requestDurationSeconds = Histogram.build()
            .name("http_request_duration_seconds").help("Duration of a request").register();

    private static Gauge inFlightRequestsTotal = Gauge.build()
            .name("http_in_flight_requests_total").help("Number of in-flight requests").register();


    @Inject
    public PrometheusFilter(Materializer mat) {
        super(mat);
    }

    @Override
    public CompletionStage<Result> apply(
            Function<Http.RequestHeader, CompletionStage<Result>> nextFilter,
            Http.RequestHeader requestHeader) {

        inFlightRequestsTotal.inc();
        Histogram.Timer requestTimer = requestDurationSeconds.startTimer();

        return nextFilter.apply(requestHeader).thenApply(result -> {
            requestsTotal.labels(requestHeader.method(), String.valueOf(result.status())).inc();
            inFlightRequestsTotal.dec();
            requestTimer.observeDuration();
            return result;
        });
    }
}
