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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

abstract public class MetricsFilterCommonTest {
    MetricsFilter f =getMetricsFilter();

    abstract public MetricsFilter getMetricsFilter();

    @After
    public void clear() {
        CollectorRegistry.defaultRegistry.clear();
    }


}
