package io.prometheus.client.spring.web;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Histogram;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.Enumeration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MethodTimerTest {
    private interface Timeable {
        void timeMe() throws Exception;
        void timeMeWithHistogram() throws Exception;
    }

    private final class TestClass implements Timeable {
        @PrometheusTimeMethod(name = "test_class", help = "help one")
        public void timeMe() throws Exception {
            Thread.sleep(20);
        }

        @PrometheusTimeMethod(name = "test_class_histogram", help = "help two", collectorClass = Histogram.class)
        public void timeMeWithHistogram() throws Exception {
            Thread.sleep(100);
        }

    }

    private interface Time2 {
        void timeMe() throws Exception;
        void timeMeWithHistogram() throws Exception;
        void aSecondMethod() throws Exception;
    }

    @Test
    public void timeMethod() throws Exception {
        Timeable cprime = new TestClass();
        AspectJProxyFactory factory = new AspectJProxyFactory(cprime);
        factory.addAspect(MethodTimer.class);
        Timeable proxy = factory.getProxy();

        proxy.timeMe();

        final Double tot = CollectorRegistry.defaultRegistry.getSampleValue("test_class_sum");
        Assert.assertNotNull(tot);
        assertEquals(0.02, tot, 0.01);

        proxy.timeMeWithHistogram();

        final Double histogramTotalFiveMillisBucket = CollectorRegistry.defaultRegistry.getSampleValue("test_class_histogram_bucket",
                new String[] { "le" }, new String[] { "0.005" });
        assertEquals(histogramTotalFiveMillisBucket, Double.valueOf(0.0));

        final Double histogramTotal500msBucket = CollectorRegistry.defaultRegistry.getSampleValue("test_class_histogram_bucket",
                new String[] { "le" }, new String[] { "0.5" });
        assertEquals(histogramTotal500msBucket, Double.valueOf(1.0));

    }

    <T> T getProxy(T source){
        AspectJProxyFactory factory = new AspectJProxyFactory(source);
        factory.addAspect(MethodTimer.class);
        return factory.getProxy();
    }

    @Test
    public void testValueParam() throws Exception {
        final String name = "foobar";
        final String nameHistogram = "foobarHistogram";
        Time2 a = getProxy(new Time2() {
            @PrometheusTimeMethod(name = name, help="help")
            @Override
            public void timeMe() throws Exception {
                Thread.sleep(35);
            }

            @Override
            @PrometheusTimeMethod(name = nameHistogram, help="help", collectorClass = Histogram.class)
            public void timeMeWithHistogram() throws Exception {
                Thread.sleep(40);
            }

            @Override
            public void aSecondMethod() throws Exception {

            }
        });

        a.timeMe();

        final Double tot = CollectorRegistry.defaultRegistry.getSampleValue(name + "_sum");
        assertEquals(0.035, tot,0.01);

        a.timeMe();
        a.timeMe();
        a.timeMe();
        final Double tot2 = CollectorRegistry.defaultRegistry.getSampleValue(name + "_sum");
        assertEquals(0.035*4, tot2, 0.1);

        a.timeMeWithHistogram();

        final Double histogramTotalOneTenthBucket = CollectorRegistry.defaultRegistry.getSampleValue(nameHistogram + "_bucket", new String[] { "le" }, new String[] { "0.1" });
        assertEquals(histogramTotalOneTenthBucket, Double.valueOf(1.0));

        final Double histogramTotOneHundredthSecond = CollectorRegistry.defaultRegistry.getSampleValue(nameHistogram + "_bucket", new String[] { "le" }, new String[] { "0.01" });
        assertEquals(histogramTotOneHundredthSecond, Double.valueOf(0.0));
    }

    @Test
    public void testHelpParam() throws Exception {
        final String name = "foo";
        final String help = "help";
        final String nameHistogram = "fooHistogram";
        final String helpHistogram = "help histogram";

        Time2 a = getProxy(new Time2() {
            @Override
            @PrometheusTimeMethod(name = name, help = help)
            public void timeMe() throws Exception {
                Thread.sleep(100);
            }

            @Override
            @PrometheusTimeMethod(name = nameHistogram, help= helpHistogram, collectorClass = Histogram.class)
            public void timeMeWithHistogram() throws Exception {
                Thread.sleep(25);
            }

            @Override
            public void aSecondMethod() throws Exception {

            }
        });

        a.timeMe();
        a.timeMeWithHistogram();

        final Enumeration<Collector.MetricFamilySamples> samples = CollectorRegistry.defaultRegistry.metricFamilySamples();

        Collector.MetricFamilySamples sampleSummary = null;
        Collector.MetricFamilySamples sampleHistogram = null;
        while (samples.hasMoreElements()) {
            Collector.MetricFamilySamples sample = samples.nextElement();
            if (name.equals(sample.name)) {
                sampleSummary = sample;
            } else if (nameHistogram.equals(sample.name)) {
                sampleHistogram = sample;
            }
        }
        Assert.assertNotNull(sampleSummary);
        assertEquals(help, sampleSummary.help);

        Assert.assertNotNull(sampleHistogram);
        assertEquals(helpHistogram, sampleHistogram.help);
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
            @PrometheusTimeMethod(name = "fooasdf2", help="help", collectorClass = Histogram.class)
            public void timeMeWithHistogram() throws Exception {
                Thread.sleep(25);
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
        assertEquals(0.01, tot, 0.01);
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
            @PrometheusTimeMethod(name = "fooasdf3", help="help", collectorClass = Histogram.class)
            public void timeMeWithHistogram() throws Exception {
                Thread.sleep(25);
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
        assertEquals(0.001*count*sleepTime, total, 0.1);

        assertNotNull(misnamedTotal);
        assertEquals(0.001*misnamedSleepTime, misnamedTotal, 0.01);
    }

    private interface SameMethodNameTest {
        void doSomething() throws Exception;
        void doSomething(String s) throws Exception;
    }

    @Test
    public void testOverloadedMethodName() throws Exception {
        final int sleep1 = 100, sleep2 = 200;
        
        SameMethodNameTest r = getProxy(new SameMethodNameTest() {
            @Override
            @PrometheusTimeMethod(name="dosomething_one_test_seconds", help = "halp")
            public void doSomething() throws Exception {
                Thread.sleep(sleep1);
            }

            @Override
            @PrometheusTimeMethod(name = "dosomething_two_test_seconds", help = "also halp")
            public void doSomething(String s) throws Exception {
                Thread.sleep(sleep2);
            }
        });
        
        r.doSomething();
        r.doSomething("foobar");

        final Double tot1 = CollectorRegistry.defaultRegistry.getSampleValue("dosomething_one_test_seconds_sum");
        final Double tot2 = CollectorRegistry.defaultRegistry.getSampleValue("dosomething_two_test_seconds_sum");

        assertEquals(.001*sleep2, tot2,.01);
        assertEquals(.001*sleep1, tot1, .01);
    }
}
