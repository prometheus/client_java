package io.prometheus.metrics.exporter.servlet.jakarta;

import io.prometheus.metrics.config.ExporterFilterProperties;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.expositionformats.ExpositionFormatWriter;
import io.prometheus.metrics.expositionformats.ExpositionFormats;
import io.prometheus.metrics.model.registry.MetricNameFilter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.function.Predicate;

/**
 * Initial example exporter so that we can try the new metrics library out.
 * <p>
 * We'll add a Jakarta servlet, the built-in HTTPServer, etc. soon, and likely move common code into a common module.
 */
public class PrometheusMetricsServlet extends HttpServlet {

    private final PrometheusRegistry registry;

    private final ExpositionFormats expositionFormats;
    private final Predicate<String> nameFilter;

    public PrometheusMetricsServlet(PrometheusProperties config, PrometheusRegistry registry) {
        this.expositionFormats = ExpositionFormats.init(config.getExporterProperties());
        this.registry = registry;
        this.nameFilter = makeNameFilter(config.getExporterFilterProperties());
    }

    private static Predicate<String> makeNameFilter(ExporterFilterProperties props) {
        if (props.getAllowedNames() == null && props.getExcludedNames() == null && props.getAllowedPrefixes() == null && props.getExcludedPrefixes() == null) {
            return null;
        } else {
            return MetricNameFilter.newBuilder()
                    .nameMustBeEqualTo(props.getAllowedNames())
                    .nameMustNotBeEqualTo(props.getExcludedNames())
                    .nameMustStartWith(props.getAllowedPrefixes())
                    .nameMustNotStartWith(props.getExcludedPrefixes())
                    .build();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Predicate<String> filter = nameFilter;
        String[] allowedNames = request.getParameterValues("name[]");
        if (allowedNames != null) {
            filter = filter.and(MetricNameFilter.newBuilder().nameMustBeEqualTo(allowedNames).build());
        }
        switch (request.getParameter("debug")) {
            case "openmetrics":
                response.setContentType("text/plain");
                expositionFormats.getOpenMetricsTextFormatWriter().write(response.getOutputStream(), registry.scrape(filter));
                break;
            case "text":
                response.setContentType("text/plain");
                expositionFormats.getPrometheusTextFormatWriter().write(response.getOutputStream(), registry.scrape(filter));
                break;
            case "prometheus-protobuf":
                response.setContentType("text/plain");
                String debugString = expositionFormats.getPrometheusProtobufWriter().toDebugString(registry.scrape(filter));
                response.getWriter().write(debugString);
                break;
            default:
                String acceptHeader = request.getHeader("Accept");
                ExpositionFormatWriter writer = expositionFormats.findWriter(acceptHeader);
                response.setContentType(writer.getContentType());
                writer.write(response.getOutputStream(), registry.scrape(filter));
        }
    }
}
