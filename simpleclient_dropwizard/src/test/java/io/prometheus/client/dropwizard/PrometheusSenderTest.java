package io.prometheus.client.dropwizard;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import io.prometheus.client.Collector;
import io.prometheus.client.exporter.PushGateway;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PrometheusSenderTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfJobIsNull() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("jobId must not be null nor empty");
        PrometheusSender.withPushGateway(mock(PushGateway.class))
                .build();
    }

    @Test
    public void shouldThrowExceptionIfJobIsEmpty() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("jobId must not be null nor empty");
        PrometheusSender.withPushGateway(mock(PushGateway.class))
                .jobId("")
                .build();
    }

    @Test
    public void shouldThrowExceptionIfPushGatewayIsNull() {
        exception.expect(NullPointerException.class);
        exception.expectMessage("pushGateway must not be null");
        PrometheusSender.withPushGateway(null)
                .jobId("job-1")
                .build();
    }

    @Test
    public void shouldThrowExceptionIfGroupKeyIsNotUnique() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Group key must be unique: key-1");
        PrometheusSender.withPushGateway(mock(PushGateway.class))
                .jobId("job-1")
                .addGroup("key-1", "value-1")
                .addGroup("key-1", "value-2")
                .build();
    }

    @Test
    public void shouldSendWithoutGroupKeys() throws Exception {

        PushGateway gateway = mock(PushGateway.class);
        Collector collector = mock(Collector.class);

        PrometheusSender sender = PrometheusSender.withPushGateway(gateway)
                .jobId("job-1")
                .build();

        sender.send(collector);

        verify(gateway).pushAdd(eq(collector), eq("job-1"), eq(Collections.<String, String>emptyMap()));
    }

    @Test
    public void shouldSendWithGroupKeys() throws Exception {

        PushGateway gateway = mock(PushGateway.class);
        Collector collector = mock(Collector.class);

        PrometheusSender sender = PrometheusSender.withPushGateway(gateway)
                .jobId("job-1")
                .addGroup("instance", "127.0.0.1")
                .addGroup("role", "master")
                .build();

        sender.send(collector);

        verify(gateway).pushAdd(eq(collector), eq("job-1"), eq(pairsOf("instance", "127.0.0.1",
                "role", "master")));

    }

    private Map<String, String> pairsOf(String... keyAndValue) {
        Map<String, String> map = new LinkedHashMap<String, String>(keyAndValue.length/2);
        for (int i = 0; i < keyAndValue.length; i+=2) {
            map.put(keyAndValue[i], keyAndValue[i+1]);
        }
        return map;
    }
}
