package io.prometheus.client.exporter;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class BasicAuthPushGateway extends PushGateway {
    private final String basicAuthHeader;

    public BasicAuthPushGateway(String address, String user, String password) {
        super(address);

        this.basicAuthHeader = encode(user, password);
    }

    public BasicAuthPushGateway(URL serverBaseURL, String user, String password) {
        super(serverBaseURL);

        this.basicAuthHeader = encode(user, password);
    }

    @Override
    protected void authenticate(HttpURLConnection connection) {
        connection.setRequestProperty("Authorization", basicAuthHeader);
    }

    private String encode(String user, String password) {
        try {
            byte[] message = (user + ":" + password).getBytes("UTF-8");
            String encoded = javax.xml.bind.DatatypeConverter.printBase64Binary(message);
            return String.format("Basic %s", encoded);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
