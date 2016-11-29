package io.prometheus.client.spring.boot;

import io.prometheus.client.Collector;
import io.prometheus.client.spring.boot.MethodTimer;
import io.prometheus.client.spring.boot.PrometheusTimeMethods;
import org.junit.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by andrew on 11/25/16.
 */
public class MethodTimerTest {
    Timeable proxy;

    private static interface Timeable {
        public void timeMe() throws Exception;
    }

    private static class TestClass implements Timeable {
        @PrometheusTimeMethods
        public void timeMe() throws Exception {
            Thread.sleep(20);
        }
    }

    private static interface Time2 {
        public void timeMe() throws Exception;
    }

    @PrometheusTimeMethods
    private static class TestClass2 implements Time2 {
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

        final List<Collector.MetricFamilySamples> samples = MethodTimer.hist.collect();

        assertNotNull(samples);
        assertEquals(samples.size(), 1);
        double tot = 0.0;
        for(Collector.MetricFamilySamples.Sample s : samples.get(0).samples) {
            if (s.name.equals(MethodTimer.METRIC_NAME + "_sum") && s.labelValues.get(0).equals("Timeable.timeMe()")) {
                tot = s.value;
            }
        }
        System.out.print(tot);
        assert(0.019 < tot && tot < 0.025);
    }

    @Test
    public void timeClassAnnotation() throws Exception {
        Time2 cprime = new TestClass2();
        AspectJProxyFactory factory = new AspectJProxyFactory(cprime);
        MethodTimer timer = new MethodTimer();
        factory.addAspect(timer);
        Time2 proxy = factory.getProxy();

        proxy.timeMe();

        final List<Collector.MetricFamilySamples> samples = MethodTimer.hist.collect();

        assertNotNull(samples);
        assertEquals(samples.size(), 1);
        double tot = 0.0;
        for(Collector.MetricFamilySamples.Sample s : samples.get(0).samples) {
            if (s.name.equals(MethodTimer.METRIC_NAME + "_sum") && s.labelValues.get(0).equals("Time2.timeMe()")) {
                tot = s.value;
            }
        }
        assert(0.029 < tot && tot < 0.035);
    }
}