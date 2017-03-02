package io.prometheus.client.spring.web;

import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.junit.Assert.*;

@ContextConfiguration
@EnablePrometheusTiming
public class MethodTimerAppTest {

    @Controller
    public static class MyController {
        @RequestMapping("/")
        @PrometheusTimeMethod(name = "prom_time_seconds", help = "time")
        public void waitJustAGoshDarnSecond() throws Exception {
            Thread.sleep(1000);
        }
    }


    public static class MyConfig {

    }
}