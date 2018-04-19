package io.prometheus.client.dropwizard;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import io.prometheus.client.Collector;
import io.prometheus.client.exporter.PushGateway;

public class PrometheusSender {

    private final PushGateway pushGateway;
    private final String jobId;
    private final Map<String, String> groupingKeys;

    private PrometheusSender(PushGateway pushGateway, String jobId, Map<String, String> groupingKeys) {
        this.pushGateway = pushGateway;
        this.jobId = jobId;
        this.groupingKeys = groupingKeys;
    }

    public void send(Collector collector) throws IOException {
        pushGateway.pushAdd(collector, jobId, groupingKeys);
    }

    public static class Builder {

        private final PushGateway pushGateway;
        private final Map<String, String> groupingKeys = new LinkedHashMap<String, String>();
        private String jobId;

        Builder(PushGateway pushGateway) {
            this.pushGateway = pushGateway;
        }

        public Builder jobId(String jobId) {
            this.jobId = jobId;
            return this;
        }

        public Builder addGroup(String key, String value) {
            if (groupingKeys.containsKey(key)) {
                throw new IllegalArgumentException("Group key must be unique: " + key);
            }
            groupingKeys.put(key, value);
            return this;
        }

        public PrometheusSender build() {
            if (jobId == null || jobId.isEmpty()) {
                throw new IllegalArgumentException("jobId must not be null nor empty");
            }
            return new PrometheusSender(pushGateway, jobId, groupingKeys);
        }

    }

    public static Builder withPushGateway(PushGateway pushGateway) {
        if (pushGateway == null) {
            throw new NullPointerException("pushGateway must not be null");
        }
        return new Builder(pushGateway);
    }
}
