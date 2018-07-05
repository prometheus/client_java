package io.prometheus.client.exporter;

public class BasicAuthPushGateway extends PushGateway {
    public BasicAuthPushGateway(String address, String user, String password) {
        super(address);

        setConnectionFactory(
            new BasicAuthHttpConnectionFactory(user, password)
        );
    }
}
