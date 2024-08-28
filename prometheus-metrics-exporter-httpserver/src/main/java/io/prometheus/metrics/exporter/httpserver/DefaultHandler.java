package io.prometheus.metrics.exporter.httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Handler for the / endpoint
 */
public class DefaultHandler implements HttpHandler {

    private final byte[] responseBytes;
    private final String contentType;

    public DefaultHandler() {
        String responseString = "" +
                "<html>\n" +
                "<head><title>Prometheus Java Client</title></head>\n" +
                "<body>\n" +
                "<h1>Prometheus Java Client</h1>\n" +
                "<h2>Metrics Path</h2>\n" +
                "The metrics path is <a href=\"metrics\">/metrics</a>.\n" +
                "<h2>Name Filter</h2>\n" +
                "If you want to scrape only specific metrics, use the <tt>name[]</tt> parameter like this:\n" +
                "<ul>\n" +
                "<li><a href=\"metrics?name[]=my_metric\">/metrics?name[]=my_metric</a></li>\n" +
                "</ul>\n" +
                "You can also use multiple <tt>name[]</tt> parameters to query multiple metrics:\n" +
                "<ul>\n" +
                "<li><a href=\"metrics?name[]=my_metric_a&name=my_metrics_b\">/metrics?name[]=my_metric_a&amp;name=[]=my_metric_b</a></li>\n" +
                "</ul>\n" +
                "The <tt>name[]</tt> parameter can be used by the Prometheus server for scraping. Add the following snippet to your scrape job configuration in <tt>prometheus.yaml</tt>:\n" +
                "<pre>\n" +
                "params:\n" +
                "    name[]:\n" +
                "        - my_metric_a\n" +
                "        - my_metric_b\n" +
                "</pre>\n" +
                "<h2>Debug Parameter</h2>\n" +
                "The Prometheus Java metrics library supports multiple exposition formats.\n" +
                "The Prometheus server sends the <tt>Accept</tt> header to indicate which format it accepts.\n" +
                "By default, the Prometheus server accepts OpenMetrics text format, unless the Prometheus server is started with feature flag <tt>--enable-feature=native-histograms</tt>,\n" +
                "in which case the default is Prometheus protobuf.\n" +
                "The Prometheus Java metrics library supports a <tt>debug</tt> query parameter for viewing the different formats in a Web browser:\n" +
                "<ul>\n" +
                "<li><a href=\"metrics?debug=openmetrics\">/metrics?debug=openmetrics</a>: View OpenMetrics text format.</li>\n" +
                "<li><a href=\"metrics?debug=text\">/metrics?debug=text</a>: View Prometheus text format (this is the default when accessing the <a href=\"metrics\">/metrics</a> endpoint with a Web browser).</li>\n" +
                "<li><a href=\"metrics?debug=prometheus-protobuf\">/metrics?debug=prometheus-protobuf</a>: View a text representation of the Prometheus protobuf format.</li>\n" +
                "</ul>\n" +
                "Note that the <tt>debug</tt> parameter is only for viewing different formats in a Web browser, it should not be used by the Prometheus server for scraping. The Prometheus server uses the <tt>Accept</tt> header for indicating which format it accepts.\n" +
                "</body>\n" +
                "</html>\n";
        this.responseBytes = responseString.getBytes(StandardCharsets.UTF_8);
        this.contentType = "text/html; charset=utf-8";
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Content-Length", Integer.toString(responseBytes.length));
            exchange.sendResponseHeaders(200, responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
        } finally {
            exchange.close();
        }
    }
}
