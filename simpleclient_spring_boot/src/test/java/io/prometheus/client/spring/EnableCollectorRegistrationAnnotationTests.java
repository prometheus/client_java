package io.prometheus.client.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.spring.CollectorRegistrationBeanPostProcessor;
import io.prometheus.client.spring.CollectorRegistryAware;
import io.prometheus.client.spring.CollectorRegistryAwareBeanPostProcessor;
import io.prometheus.client.spring.CollectorRegistryFactoryBean;
import io.prometheus.client.spring.EnablePrometheusCollectorRegistration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Stuart Williams (pidster)
 */
@RunWith(SpringRunner.class)
public class EnableCollectorRegistrationAnnotationTests {

    @Autowired
    ApplicationContext context;

    @Test
    public void testEnableAnnotation() {
        assertThat(context.getBeansOfType(CollectorRegistryFactoryBean.class)).hasSize(1);
        assertThat(context.getBeansOfType(CollectorRegistryAwareBeanPostProcessor.class)).hasSize(1);
        assertThat(context.getBeansOfType(CollectorRegistrationBeanPostProcessor.class)).hasSize(1);
        assertThat(context.getBeansOfType(TestCollectorRegistryAware.class)).hasSize(1);

        TestCollectorRegistryAware aware = context.getBean(TestCollectorRegistryAware.class);
        assertNotNull(aware.collectorRegistry);

        CollectorRegistry registry = context.getBean(CollectorRegistry.class);
        assertNotNull(registry);
        assertNotEquals(CollectorRegistry.defaultRegistry, registry);

        List<Sample> sampleList = new ArrayList<Sample>();

        Enumeration<MetricFamilySamples> samplesEnum = registry.metricFamilySamples();
        while (samplesEnum.hasMoreElements()) {
            MetricFamilySamples element = samplesEnum.nextElement();
            sampleList.addAll(element.samples);
        }

        assertThat(sampleList).hasSize(1);
        assertEquals("testnamespace_testsubsystem_testname", sampleList.get(0).name);
    }

    @Test
    public void testEnableAnnotationClasses() {
        assertThat(context.getBeansOfType(CollectorRegistryFactoryBean.class)).hasSize(1);
        assertThat(context.getBeansOfType(CollectorRegistry.class)).hasSize(1);
        assertThat(context.getBeansOfType(CollectorRegistryAwareBeanPostProcessor.class)).hasSize(1);
        assertThat(context.getBeansOfType(CollectorRegistrationBeanPostProcessor.class)).hasSize(1);
    }

    @Test
    public void testCollectorRegistryAware() {
        TestCollectorRegistryAware aware = context.getBean(TestCollectorRegistryAware.class);
        assertNotNull(aware.collectorRegistry);

        CollectorRegistry registry = context.getBean(CollectorRegistry.class);
        assertNotNull(registry);
        assertNotEquals(CollectorRegistry.defaultRegistry, registry);
    }

    @Test
    public void testCollectorRegistration() {
        List<Sample> sampleList = new ArrayList<Sample>();

        CollectorRegistry registry = context.getBean(CollectorRegistry.class);
        Enumeration<MetricFamilySamples> samplesEnum = registry.metricFamilySamples();
        while (samplesEnum.hasMoreElements()) {
            MetricFamilySamples element = samplesEnum.nextElement();
            sampleList.addAll(element.samples);
        }

        assertThat(sampleList).hasSize(1);
        assertEquals("testnamespace_testsubsystem_testname", sampleList.get(0).name);
    }

    @Configuration
    @EnablePrometheusCollectorRegistration(useDefault = false)
    static class TestConfiguration {

        @Bean
        public TestCollectorRegistryAware aware() {
            return new TestCollectorRegistryAware();
        }

        @Bean
        public Counter counter() {
            return new Counter.Builder()
                    .namespace("testnamespace")
                    .subsystem("testsubsystem")
                    .name("testname")
                    .help("Help for testname")
                    .create();
        }

    }

    static class TestCollectorRegistryAware implements CollectorRegistryAware {

        private CollectorRegistry collectorRegistry;

        @Override
        public void setCollectorRegistry(CollectorRegistry collectorRegistry) {
            this.collectorRegistry = collectorRegistry;
        }
    }

}
