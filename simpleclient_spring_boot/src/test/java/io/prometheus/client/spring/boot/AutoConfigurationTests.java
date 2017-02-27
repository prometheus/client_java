package io.prometheus.client.spring.boot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Stuart Williams (pidster)
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class AutoConfigurationTests {

    private static final Logger logger = LoggerFactory.getLogger(AutoConfigurationTests.class);

    @Autowired
    ApplicationContext context;

    @Autowired
    SecurityProperties securityProperties;

    @LocalServerPort
    int port;

    @Value("${endpoints.prometheus.path}")
    String prometheusPath;

    @Test
    public void testPrometheusEndpointPath() {

        assertThat(context.getBeansOfType(PrometheusEndpoint.class)).hasSize(1);
        assertThat(context.getBeansOfType(PrometheusMvcEndpoint.class)).hasSize(1);

        RestTemplate restTemplate = new RestTemplateBuilder()
                .basicAuthorization("user", securityProperties.getUser().getPassword())
                .build();

        String resultProm = restTemplate.getForObject("http://localhost:" + port + prometheusPath, String.class);

        logger.debug("Dump of metrics...\n{}", resultProm);

        assertThat(resultProm).isNotEmpty();

        assertThat(resultProm).contains("process_start_time_seconds");
        assertThat(resultProm).contains("jvm_threads_current");
        assertThat(resultProm).contains("jvm_threads_daemon");
        assertThat(resultProm).contains("jvm_classes_loaded_total");
        assertThat(resultProm).contains("jvm_classes_unloaded_total");
        assertThat(resultProm).contains("jvm_spring_classes_loaded");
        assertThat(resultProm).contains("jvm_spring_processors");
        assertThat(resultProm).contains("jvm_spring_httpsessions_active");
        assertThat(resultProm).contains("jvm_spring_httpsessions_max");
    }


    @SpringBootApplication
    public static class TestConfiguration {

    }
}
