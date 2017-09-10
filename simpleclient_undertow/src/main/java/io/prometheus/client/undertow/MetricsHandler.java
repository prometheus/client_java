package io.prometheus.client.undertow;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.util.Headers;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Metrics Handler for Undertow.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * Undertow.builder()
 *      .addHttpListener(port, host)
 *      .setHandler(path().addExactPath("/metrics", new MetricsHandler()))
 *      .build();
 * }
 * </pre>
 */
public class MetricsHandler implements HttpHandler {

    private final HttpHandler handler;

    /**
     * Construct a MetricsHandler for the default registry
     */
    public MetricsHandler() {
        this(CollectorRegistry.defaultRegistry);
    }

    /**
     * Construct a MetricsHandler for the given registry
     *
     * @param registry collector registry
     */
    public MetricsHandler(CollectorRegistry registry) {
        handler = new BlockingHandler(new Handler(registry));
    }


    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        handler.handleRequest(exchange);
    }

    private class Handler implements HttpHandler {

        private static final String NAME_PARAMETER = "name[]";

        private final CollectorRegistry registry;

        public Handler(CollectorRegistry registry) {
            this.registry = registry;
        }

        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, TextFormat.CONTENT_TYPE_004);
            try (Writer writer = new OutputStreamWriter(exchange.getOutputStream())) {
                TextFormat.write004(writer, registry.filteredMetricFamilySamples(parse(exchange.getQueryParameters())));
                writer.flush();
            }
        }

        private Set<String> parse(Map<String, Deque<String>> params) {
            Deque<String> includedParam = params.get(NAME_PARAMETER);
            if (includedParam == null) {
                return Collections.emptySet();
            } else {
                return new HashSet<>(includedParam);
            }
        }
    }
}
