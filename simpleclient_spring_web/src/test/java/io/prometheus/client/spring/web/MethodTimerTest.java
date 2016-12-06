package io.prometheus.client.spring.web;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Histogram;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.Enumeration;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by andrew on 11/25/16.
 */
@Configuration
@EnableAspectJAutoProxy
 public class MethodTimerTest {
    Timeable proxy;

    private interface Timeable {
        void timeMe() throws Exception;
    }

    private final class TestClass implements Timeable {
        @PrometheusTimeMethods
        public void timeMe() throws Exception {
            Thread.sleep(20);
        }
    }

    private interface Time2 {
        void timeMe() throws Exception;
    }

    @PrometheusTimeMethods
    private final class TestClass2 implements Time2 {
        public void timeMe() throws  Exception {
            Thread.sleep(30);
        }
    }

    private TestClass c;

    @Test
    public void timeMethod() throws Exception {
        Timeable cprime = new TestClass();
        AspectJProxyFactory factory = new AspectJProxyFactory(cprime);
        MethodTimer timer = new MethodTimer();
        factory.addAspect(timer);
        Timeable proxy = factory.getProxy();

        proxy.timeMe();

        final List<Collector.MetricFamilySamples> samples = MethodTimer.defaultSummary.collect();

        Assert.assertNotNull(samples);
        assertEquals(samples.size(), 1);
        final Double tot = CollectorRegistry.defaultRegistry.getSampleValue(MethodTimer.METRIC_NAME + "_sum", new String[]{"signature"}, new String[]{"Timeable.timeMe()"});
        Assert.assertNotNull(tot);
        assertEquals(0.02, tot, 0.001);
    }

    Time2 getProxy(Time2 source){
        AspectJProxyFactory factory = new AspectJProxyFactory(source);
        MethodTimer timer = new MethodTimer();
        factory.addAspect(timer);
        return factory.getProxy();
    }

    @Test
    public void timeClassAnnotation() throws Exception {
        Time2 proxy = getProxy(new TestClass2());

        proxy.timeMe();

        final List<Collector.MetricFamilySamples> samples = MethodTimer.defaultSummary.collect();

        Assert.assertNotNull(samples);
        assertEquals(samples.size(), 1);

        final Double tot = CollectorRegistry.defaultRegistry.getSampleValue(MethodTimer.METRIC_NAME + "_sum", new String[]{"signature"}, new String[]{"Time2.timeMe()"});
        Assert.assertNotNull(tot);
        assertEquals(tot, 0.03, 0.001);
        assert(0.029 < tot && tot < 0.031);
    }

    @Test
    public void testValueParam() throws Exception {
        final String name = "foobar";
        Time2 a = getProxy(new Time2() {
            @PrometheusTimeMethods(name)
            @Override
            public void timeMe() throws Exception {
                Thread.sleep(35);
            }
        });

        a.timeMe();

        final Double tot = CollectorRegistry.defaultRegistry.getSampleValue(name + "_sum", new String[]{"signature"}, new String[]{"Time2.timeMe()"});
        assertEquals(tot, 0.035, 0.001);
    }

    @Test
    public void testHelpParam() throws Exception {
        final String name = "foo";
        final String help = "help";

        Time2 a = getProxy(new Time2() {
            @Override
            @PrometheusTimeMethods(value = name, help = help)
            public void timeMe() throws Exception {
                Thread.sleep(100);
            }
        });

        a.timeMe();

        final Enumeration<Collector.MetricFamilySamples> samples = CollectorRegistry.defaultRegistry.metricFamilySamples();

        Collector.MetricFamilySamples sample = null;
        while (samples.hasMoreElements()) {
            sample = samples.nextElement();
            if (name.equals(sample.name)) {
                break;
            }
        }
        Assert.assertNotNull(sample);
        assertEquals(sample.help, help);
    }
}