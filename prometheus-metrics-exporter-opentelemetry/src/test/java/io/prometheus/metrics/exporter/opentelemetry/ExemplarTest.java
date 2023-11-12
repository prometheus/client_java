package io.prometheus.metrics.exporter.opentelemetry;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.ValueMatcher;
import com.google.protobuf.InvalidProtocolBufferException;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.metrics.v1.InstrumentationLibraryMetrics;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.NumberDataPoint;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class ExemplarTest {
    private static final String ENDPOINT_PATH = "/v1/metrics";
    private static final int TIMEOUT = 3;
    private static final String INSTRUMENTATION_SCOPE_NAME = "testInstrumentationScope";
    private static final String SPAN_NAME = "test-span";
    public static final String TEST_COUNTER_NAME = "test_counter";
    private Counter testCounter;
    private OpenTelemetryExporter openTelemetryExporter;
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(4317);

    @Before
    public void setUp() {
        openTelemetryExporter = OpenTelemetryExporter.builder()
                .endpoint("http://localhost:4317")
                .protocol("http/protobuf")
                .intervalSeconds(1)
                .buildAndStart();

        testCounter = Counter.builder()
                .name(TEST_COUNTER_NAME)
                .withExemplars()
                .register();

        wireMockRule.stubFor(post(ENDPOINT_PATH)
                .withHeader("Content-Type", containing("application/x-protobuf"))
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"partialSuccess\":{}}")));
    }

    @After
    public void tearDown() {
        PrometheusRegistry.defaultRegistry.unregister(testCounter);
        openTelemetryExporter.close();
    }

    @Test
    public void sampledExemplarIsForwarded() {
         try (SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                    .setSampler(Sampler.alwaysOn())
                    .build()) {

                Tracer test = sdkTracerProvider.get(INSTRUMENTATION_SCOPE_NAME);
                Span span = test.spanBuilder(SPAN_NAME)
                        .startSpan();
                try (Scope scope = span.makeCurrent()) {
                    testCounter.inc(2);
                }
            }


            await().atMost(TIMEOUT, SECONDS)
                    .ignoreException(com.github.tomakehurst.wiremock.client.VerificationException.class)
                    .until(() -> {
                        verify(postRequestedFor(urlEqualTo(ENDPOINT_PATH))
                                .withHeader("Content-Type", equalTo("application/x-protobuf"))
                                .andMatching(getExemplarCountMatcher(1)));
                        return true;
                    });

    }

    @Test(expected = ConditionTimeoutException.class)
    public void notSampledExemplarIsNotForwarded() {
        try (SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .setSampler(Sampler.alwaysOff())
                .build()) {

            Tracer test = sdkTracerProvider.get(INSTRUMENTATION_SCOPE_NAME);
            Span span = test.spanBuilder(SPAN_NAME)
                    .startSpan();
            try (Scope scope = span.makeCurrent()) {
                testCounter.inc(2);
            }
        }

        await().atMost(TIMEOUT, SECONDS)
                .ignoreException(com.github.tomakehurst.wiremock.client.VerificationException.class)
                .until(() -> {
                    verify(postRequestedFor(urlEqualTo(ENDPOINT_PATH))
                            .withHeader("Content-Type", equalTo("application/x-protobuf"))
                            .andMatching(getExemplarCountMatcher(1)));
                    return true;
                });

    }

    private static ValueMatcher<Request> getExemplarCountMatcher(int expectedCount) {
        return request -> {
            try {
                ExportMetricsServiceRequest exportMetricsServiceRequest = ExportMetricsServiceRequest.parseFrom(request.getBody());
                for (ResourceMetrics resourceMetrics : exportMetricsServiceRequest.getResourceMetricsList()) {
                    for (InstrumentationLibraryMetrics instrumentationLibraryMetrics : resourceMetrics.getInstrumentationLibraryMetricsList()) {
                        for (Metric metric : instrumentationLibraryMetrics.getMetricsList()) {
                            for (NumberDataPoint numberDataPoint : metric.getSum().getDataPointsList()) {
                                if (numberDataPoint.getExemplarsCount() == expectedCount) {
                                    return MatchResult.exactMatch();
                                }
                            }
                        }
                    }
                }
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
            return MatchResult.noMatch();
        };
    }
}
