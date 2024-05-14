package io.prometheus.metrics.exporter.pushgateway;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

public class BasicAuthHttpConnectionFactory implements HttpConnectionFactory {
    private final HttpConnectionFactory originConnectionFactory;
    private final String basicAuthHeader;

    public BasicAuthHttpConnectionFactory(HttpConnectionFactory connectionFactory, String user, String password) {
        this.originConnectionFactory = connectionFactory;
        this.basicAuthHeader = encode(user, password);
    }

    public BasicAuthHttpConnectionFactory(String user, String password) {
        this(new DefaultHttpConnectionFactory(), user, password);
    }

    @Override
    public HttpURLConnection create(String url) throws IOException {
        HttpURLConnection connection = originConnectionFactory.create(url);
        connection.setRequestProperty("Authorization", basicAuthHeader);
        return connection;
    }

    private String encode(String user, String password) {
        try {
            byte[] credentialsBytes = (user + ":" + password).getBytes("UTF-8");
            String encoded = Base64.encodeToString(credentialsBytes);
            return String.format("Basic %s", encoded);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
