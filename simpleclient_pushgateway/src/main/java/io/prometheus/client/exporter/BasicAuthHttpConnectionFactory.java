package io.prometheus.client.exporter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import javax.xml.bind.DatatypeConverter;

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
            String encoded = DatatypeConverter.printBase64Binary(credentialsBytes);
            return String.format("Basic %s", encoded);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
