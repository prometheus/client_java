package io.prometheus.client.filter;

import io.prometheus.client.CollectorRegistry;
import org.eclipse.jetty.http.HttpMethods;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Created by andrewstuart on 11/28/16.
 */
public class MetricsFilterTest {
    MetricsFilter f = new MetricsFilter();

    @Test
    public void init() throws Exception {
        FilterConfig cfg = mock(FilterConfig.class);
        when(cfg.getInitParameter(MetricsFilter.PATH_COMPONENT_PARAM)).thenReturn("4");

        f.init(cfg);

        assertEquals(f.getPathComponents(), 4);

                HttpServletRequest req = mock(HttpServletRequest.class);

        when(req.getRequestURI()).thenReturn("/foo/bar/baz/bang/zilch/zip/nada");
        when(req.getMethod()).thenReturn(HttpMethods.GET);

        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain c = mock(FilterChain.class);

        f.doFilter(req, res, c);

        verify(c).doFilter(req, res);


        final Double sampleValue = CollectorRegistry.defaultRegistry.getSampleValue(MetricsFilter.FILTER_NAME + "_count", new String[]{"path", "verb"}, new String[]{"/foo/bar/baz/bang", HttpMethods.GET});
        assertNotNull(sampleValue);
        assertEquals(sampleValue, 1, 0.0001);
    }

    @Test
    public void doFilter() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/foo/bar/baz/bang/zilch/zip/nada");
        when(req.getMethod()).thenReturn(HttpMethods.GET);

        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain c = mock(FilterChain.class);

        f.init(null);
        f.doFilter(req, res, c);

        verify(c).doFilter(req, res);


        final Double sampleValue = CollectorRegistry.defaultRegistry.getSampleValue(MetricsFilter.FILTER_NAME + "_count", new String[]{"path", "verb"}, new String[]{"/foo/bar/baz", HttpMethods.GET});
        assertNotNull(sampleValue);
        assertEquals(sampleValue, 1, 0.0001);
    }

}