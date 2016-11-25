package io.prometheus.client.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import io.prometheus.client.hotspot.ClassLoadingExports;
import io.prometheus.client.hotspot.GarbageCollectorExports;
import io.prometheus.client.hotspot.MemoryPoolsExports;
import io.prometheus.client.hotspot.StandardExports;
import io.prometheus.client.hotspot.ThreadExports;
import io.prometheus.client.spring.EnablePrometheusCollectorRegistration;
import io.prometheus.client.spring.EnablePrometheusMetrics;

import static org.junit.Assert.assertNotNull;

/**
 * @author Stuart Williams (pidster)
 */
@RunWith(SpringRunner.class)
public class EnableMetricsAnnotationTests {

    @Autowired
    ApplicationContext context;

    @Test
    public void testJvmCollectors() {
        assertNotNull(context.getBean(ClassLoadingExports.class));
        assertNotNull(context.getBean(GarbageCollectorExports.class));
        assertNotNull(context.getBean(MemoryPoolsExports.class));
        assertNotNull(context.getBean(StandardExports.class));
        assertNotNull(context.getBean(ThreadExports.class));
    }


    @Configuration
    @EnablePrometheusCollectorRegistration
    @EnablePrometheusMetrics(spring = false)
    static class TestConfiguration {

        // 'spring' is tested separately

    }

}
