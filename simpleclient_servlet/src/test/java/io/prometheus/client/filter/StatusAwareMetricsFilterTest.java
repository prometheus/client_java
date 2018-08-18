package io.prometheus.client.filter;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import org.eclipse.jetty.http.HttpMethods;
import org.junit.After;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class StatusAwareMetricsFilterTest extends MetricsFilterCommonTest {
    public MetricsFilter getMetricsFilter(){
        return new StatusAwareMetricsFilter();
    }

    @Test
    public void testStatusCode() throws Exception {
        Map<String, Integer> sampleStatusCodes = new HashMap<String, Integer>();
        sampleStatusCodes.put("/a/page/that/exists", HttpServletResponse.SC_OK);
        sampleStatusCodes.put("/a/page/that/doesn-t-exist", HttpServletResponse.SC_NOT_FOUND);
        sampleStatusCodes.put("/a/page/that/crashes", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        FilterConfig cfg = mock(FilterConfig.class);
        when(cfg.getInitParameter(anyString())).thenReturn(null);

        String metricName = "foo";

        when(cfg.getInitParameter(MetricsFilter.METRIC_NAME_PARAM)).thenReturn(metricName);
        when(cfg.getInitParameter(MetricsFilter.PATH_COMPONENT_PARAM)).thenReturn("4");

        f.init(cfg);

        for (String uri : sampleStatusCodes.keySet()) {
            HttpServletRequest req = mock(HttpServletRequest.class);

            when(req.getRequestURI()).thenReturn(uri);
            when(req.getMethod()).thenReturn(HttpMethods.GET);

            HttpServletResponse res = mock(HttpServletResponse.class);
            when(res.getStatus()).thenReturn(sampleStatusCodes.get(uri));

            FilterChain c = mock(FilterChain.class);

            f.doFilter(req, res, c);

            verify(c).doFilter(req, res);
        }

        for (String uri : sampleStatusCodes.keySet()) {

            final Double sampleValue = CollectorRegistry.defaultRegistry
                    .getSampleValue(metricName + "_count",
                            new String[]{"path", "method", "status"},
                            new String[]{uri, HttpMethods.GET,
                                    Integer.toString(sampleStatusCodes.get(uri))});
            assertNotNull(sampleValue);
            assertEquals(1, sampleValue, 0.0001);
        }
    }

}
