package io.prometheus.client.servlet.common.filter;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.prometheus.client.servlet.common.adapter.FilterConfigAdapter;
import io.prometheus.client.servlet.common.adapter.HttpServletRequestAdapter;
import io.prometheus.client.servlet.common.adapter.HttpServletResponseAdapter;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FilterTest {
	
	public static final String GET = "GET";
	public static final String POST = "POST";
    public static final String METRIC_NAME = "foo";

    private FilterConfigAdapter mockFilterConfig() {
        return mockFilterConfig(null, null);
    }

    private FilterConfigAdapter mockFilterConfig(final String metricName, final String pathComponents) {
        return mockFilterConfig(metricName, pathComponents, null);
    }

    private FilterConfigAdapter mockFilterConfig(final String metricName, final String pathComponents, final String buckets) {
        return new FilterConfigAdapter() {
            @Override
            public String getInitParameter(String name) {
                if (Filter.METRIC_NAME_PARAM.equals(name)) {
                    return metricName;
                }
                if (Filter.PATH_COMPONENT_PARAM.equals(name)) {
                    return pathComponents;
                }
                if (Filter.BUCKET_CONFIG_PARAM.equals(name)) {
                    return buckets;
                }
                return null;
            }
        };
    }

    private HttpServletRequestAdapter mockHttpServletRequest(final String method, final String uri) {
        return new HttpServletRequestAdapter() {
            @Override
            public String getHeader(String name) {
                return null;
            }

            @Override
            public String getMethod() {
                return method;
            }

            @Override
            public String getRequestURI() {
                return uri;
            }

            @Override
            public String[] getParameterValues(String name) {
                return null;
            }

            @Override
            public String getContextPath() {
                return "";
            }
        };
    }

    private HttpServletResponseAdapter mockHttpServletResponse() {
        return mockHttpServletResponse(0);
    }

    private HttpServletResponseAdapter mockHttpServletResponse(final int status) {
        return new HttpServletResponseAdapter() {
            @Override
            public int getStatus() {
                return status;
            }

            @Override
            public void setStatus(int httpStatusCode) {
            }

            @Override
            public void setContentType(String contentType) {
            }

            @Override
            public PrintWriter getWriter() throws IOException {
                return null;
            }
        };
    }


    @After
    public void clear() {
        CollectorRegistry.defaultRegistry.clear();
    }

    @Test
    public void testPathComponents4() throws Exception {
        Filter filter = new Filter();
        String metricName = "foo";
        int pathComponents = 4;
        filter.init(mockFilterConfig(metricName, Integer.toString(pathComponents)));
        assertEquals(filter.pathComponents, 4);
        Filter.MetricData data = filter.startTimer(mockHttpServletRequest(GET, "/foo/bar/baz/bang/zilch/zip/nada"));
        filter.observeDuration(data, mockHttpServletResponse());

        final Double sampleValue = CollectorRegistry.defaultRegistry.getSampleValue(METRIC_NAME + "_count", new String[]{"path", "method"}, new String[]{"/foo/bar/baz/bang", GET});
        assertNotNull(sampleValue);
        assertEquals(1, sampleValue, 0.0001);
    }

    @Test
    public void testPathComponents0() throws Exception {
        Filter filter = new Filter();
        String metricName = "foo";
        int pathComponents = 0;
        String path = "/foo/bar/baz/bang/zilch/zip/nada";
        filter.init(mockFilterConfig(metricName, Integer.toString(pathComponents)));
        assertEquals(filter.pathComponents, 0);
        Filter.MetricData data = filter.startTimer(mockHttpServletRequest(GET, path));
        filter.observeDuration(data, mockHttpServletResponse());

        final Double sampleValue = CollectorRegistry.defaultRegistry.getSampleValue(metricName + "_count", new String[]{"path", "method"}, new String[]{path, GET});
        assertNotNull(sampleValue);
        assertEquals(1, sampleValue, 0.0001);
    }

    @Test
    public void testConstructor() throws Exception {
        final String path = "/foo/bar/baz/bang";
        HttpServletRequestAdapter req = mockHttpServletRequest(POST, path);
        HttpServletResponseAdapter res = mockHttpServletResponse();

        Filter constructed = new Filter(
                "foobar_baz_filter_duration_seconds",
                "Help for my filter",
                0,
                null,
                false
        );
        constructed.init(mockFilterConfig(null, null));

        Filter.MetricData data = constructed.startTimer(req);
        Thread.sleep(100);
        constructed.observeDuration(data, res);

        final Double sum = CollectorRegistry.defaultRegistry.getSampleValue("foobar_baz_filter_duration_seconds_sum", new String[]{"path", "method"}, new String[]{path, POST});
        assertNotNull(sum);
        assertEquals(0.1, sum, 0.01);
    }

    @Test
    public void testBucketsAndName() throws Exception {
        final String path = "/foo/bar/baz/bang";
        HttpServletRequestAdapter req = mockHttpServletRequest(POST, path);

        final String buckets = "0.01,0.05,0.1,0.15,0.25";
        FilterConfigAdapter cfg = mockFilterConfig("foo", null, buckets);

        HttpServletResponseAdapter res = mockHttpServletResponse();

        Filter filter = new Filter();
        filter.init(cfg);

        Filter.MetricData data = filter.startTimer(req);
        Thread.sleep(100);
        filter.observeDuration(data, res);

        final Double sum = CollectorRegistry.defaultRegistry.getSampleValue("foo_sum", new String[]{"path", "method"}, new String[]{"/foo", POST});
        assertEquals(0.1, sum, 0.01);

        final Double le05 = CollectorRegistry.defaultRegistry.getSampleValue("foo_bucket", new String[]{"path", "method", "le"}, new String[]{"/foo", POST, "0.05"});
        assertNotNull(le05);
        assertEquals(0, le05, 0.01);
        final Double le15 = CollectorRegistry.defaultRegistry.getSampleValue("foo_bucket", new String[]{"path", "method", "le"}, new String[]{"/foo", POST, "0.15"});
        assertNotNull(le15);
        assertEquals(1, le15, 0.01);


        final Enumeration<Collector.MetricFamilySamples> samples = CollectorRegistry.defaultRegistry.metricFamilySamples();
        Collector.MetricFamilySamples sample = null;
        while(samples.hasMoreElements()) {
            sample = samples.nextElement();
            if (sample.name.equals("foo")) {
                break;
            }
        }

        assertNotNull(sample);

        int count = 0;
        for (Collector.MetricFamilySamples.Sample s : sample.samples) {
            if (s.name.equals("foo_bucket")) {
                count++;
            }
        }
        // +1 because of the final le=+infinity bucket
        assertEquals(buckets.split(",").length+1, count);
    }

    @Test
    public void testStatusCode() throws Exception {
        HttpServletRequestAdapter req = mockHttpServletRequest(GET, "/foo/bar/baz/bang");
        HttpServletResponseAdapter res = mockHttpServletResponse(200);

        Filter constructed = new Filter(
                "foobar_filter",
                "Help for my filter",
                2,
                null,
                false
        );
        constructed.init(mockFilterConfig());

        Filter.MetricData data = constructed.startTimer(req);
        constructed.observeDuration(data, res);

        final Double sampleValue = CollectorRegistry.defaultRegistry.getSampleValue("foobar_filter_status_total", new String[]{"path", "method", "status"}, new String[]{"/foo/bar", GET, "200"});
        assertNotNull(sampleValue);
        assertEquals(1, sampleValue, 0.0001);
    }
}
