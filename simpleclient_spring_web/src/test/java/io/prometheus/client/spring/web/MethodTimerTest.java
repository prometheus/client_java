package io.prometheus.client.spring.web;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.context.annotation.Import;

import java.util.Enumeration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MethodTimerTest {
    private interface Timeable {
        void timeMe() throws Exception;
    }

    private final class TestClass implements Timeable {
        @PrometheusTimeMethod(name = "test_class", help = "help one")
        public void timeMe() throws Exception {
            Thread.sleep(20);
        }

    }

    private interface Time2 {
        void timeMe() throws Exception;
        void aSecondMethod() throws Exception;
    }

//    @PrometheusTimeMethod(name = "test_two", help = "help two")
//    private final class TestClass2 implements Time2 {
//        public void timeMe() throws  Exception {
//            Thread.sleep(30);
//        }
//    }

    @Test
    public void timeMethod() throws Exception {
        Timeable cprime = new TestClass();
        AspectJProxyFactory factory = new AspectJProxyFactory(cprime);
        factory.addAspect(MethodTimer.class);
        Timeable proxy = factory.getProxy();

        proxy.timeMe();

        final Double tot = CollectorRegistry.defaultRegistry.getSampleValue("test_class_sum");
        Assert.assertNotNull(tot);
        assertEquals(0.02, tot, 0.001);
    }

    Time2 getProxy(Time2 source){
        AspectJProxyFactory factory = new AspectJProxyFactory(source);
        factory.addAspect(MethodTimer.class);
        return factory.getProxy();
    }

//    @Test
//    public void timeClassAnnotation() throws Exception {
//        Time2 proxy = getProxy(new TestClass2());
//
//        proxy.timeMe();
//
//        final Double tot = CollectorRegistry.defaultRegistry.getSampleValue("test_two_sum");
//        Assert.assertNotNull(tot);
//        assertEquals(tot, 0.03, 0.001);
//        assert(0.029 < tot && tot < 0.031);
//    }

    @Test
    public void testValueParam() throws Exception {
        final String name = "foobar";
        Time2 a = getProxy(new Time2() {
            @PrometheusTimeMethod(name = name, help="help")
            @Override
            public void timeMe() throws Exception {
                Thread.sleep(35);
            }

            @Override
            public void aSecondMethod() throws Exception {

            }
        });

        a.timeMe();

        final Double tot = CollectorRegistry.defaultRegistry.getSampleValue(name + "_sum");
        assertEquals(tot, 0.035, 0.001);

        a.timeMe();
        a.timeMe();
        a.timeMe();
        final Double tot2 = CollectorRegistry.defaultRegistry.getSampleValue(name + "_sum");
        assertEquals(tot2, 0.035*4, 0.008);
    }

    @Test
    public void testHelpParam() throws Exception {
        final String name = "foo";
        final String help = "help";

        Time2 a = getProxy(new Time2() {
            @Override
            @PrometheusTimeMethod(name = name, help = help)
            public void timeMe() throws Exception {
                Thread.sleep(100);
            }

            @Override
            public void aSecondMethod() throws Exception {

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

    private class MyException extends Exception {
        public MyException(String msg) {
            super(msg);
        }
    }

    @Test
    public void testThrowWorks() {
        Time2 p = getProxy(new Time2() {
            @Override
            @PrometheusTimeMethod(name="fooasdf", help="bar")
            public void timeMe() throws Exception {
                Thread.sleep(10);
                throw new MyException("Yo this is an exception");
            }

            @Override
            public void aSecondMethod() throws Exception {
            }
        });

        MyException e = null;

        try {
            p.timeMe();
        } catch (Exception e1) {
            e = (MyException) e1;
        }


        final Double tot = CollectorRegistry.defaultRegistry.getSampleValue("fooasdf_sum");
        assertEquals(tot, 0.01, 0.001);
        assert(e != null);
    }

    @Test
    public void testSecondMethod() throws Exception {
        final int sleepTime = 90, misnamedSleepTime = 10;

        Time2 p = getProxy(new Time2() {
            @Override
            @PrometheusTimeMethod(name="fooasdf2", help="bar")
            public void timeMe() throws Exception {
                Thread.sleep(misnamedSleepTime);
            }

            @Override
            @PrometheusTimeMethod(name = "second_method_name_seconds", help = "help two")
            public void aSecondMethod() throws Exception {
                Thread.sleep(sleepTime);
            }
        });

        p.timeMe();

        int count = 5;
        for (int i = 0; i < count; i++) {
            p.aSecondMethod();
        }

        final Double misnamedTotal = CollectorRegistry.defaultRegistry.getSampleValue("fooasdf2_sum");
        final Double total = CollectorRegistry.defaultRegistry.getSampleValue("second_method_name_seconds_sum");

        assertNotNull(total);
        assertEquals(total, 0.001*count*sleepTime, 0.001);

        assertNotNull(misnamedTotal);
        assertEquals(misnamedTotal, 0.001*misnamedSleepTime, 0.001);

    }
}