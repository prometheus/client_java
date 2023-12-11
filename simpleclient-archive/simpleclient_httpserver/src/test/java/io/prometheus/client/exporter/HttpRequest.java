package io.prometheus.client.exporter;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Class to perform HTTP testing
 */
public class HttpRequest {

    enum METHOD { GET, HEAD }

    private final Configuration configuration;

    /**
     * Constructor
     *
     * @param configuration configuration
     */
    private HttpRequest(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Method to execute an HTTP request
     *
     * @return HttpResponse
     * @throws IOException
     */
    public HttpResponse execute() throws IOException {
        if (configuration.url.toLowerCase().startsWith("https://") && (configuration.trustManagers != null)) {
            try {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, configuration.trustManagers, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            } catch (GeneralSecurityException e) {
                throw new IOException(e);
            }

            if (configuration.hostnameVerifier != null) {
                HttpsURLConnection.setDefaultHostnameVerifier(configuration.hostnameVerifier);
            }
        }

        URLConnection urlConnection = new URL(configuration.url).openConnection();
        ((HttpURLConnection) urlConnection).setRequestMethod(configuration.method.toString());

        Set<Map.Entry<String, List<String>>> entries = configuration.headers.entrySet();
        for (Map.Entry<String, List<String>> entry : entries) {
            for (String value : entry.getValue()) {
                urlConnection.addRequestProperty(entry.getKey(), value);
            }
        }

        urlConnection.setUseCaches(false);
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.connect();

        Scanner scanner = new Scanner(urlConnection.getInputStream(), "UTF-8").useDelimiter("\\A");

        return new HttpResponse(
                ((HttpURLConnection) urlConnection).getResponseCode(),
                urlConnection.getHeaderFields(),
                        urlConnection.getContentLength(), scanner.hasNext() ? scanner.next() : "");
    }

    /**
     * Class to build an HttpRequest
     */
    static class Builder {

        private final Configuration configuration;

        /**
         * Constructor
         */
        public Builder() {
            configuration = new Configuration();
        }

        /**
         * Method to set the HTTP request method
         *
         * @param method
         * @return Builder
         */
        public Builder withMethod(METHOD method) {
            configuration.method = method;
            return this;
        }

        /**
         * Method to set the HTTP request URL
         *
         * @param url
         * @return Builder
         */
        public Builder withURL(String url) {
            configuration.url = url;
            return this;
        }

        /**
         * Method to add an HTTP request header
         *
         * @param name
         * @param value
         * @return Builder
         */
        public Builder withHeader(String name, String value) {
            configuration.addHeader(name, value);
            return this;
        }

        /**
         * Method to set the HTTP request "Authorization" header
         *
         * @param username
         * @param password
         * @return Builder
         */
        public Builder withAuthorization(String username, String password) {
            configuration.setHeader("Authorization", encodeCredentials(username, password));
            return this;
        }

        /**
         * Method to set the HTTP request trust managers when using an SSL URL
         *
         * @param trustManagers
         * @return Builder
         */
        public Builder withTrustManagers(TrustManager[] trustManagers) {
            configuration.trustManagers = trustManagers;
            return this;
        }

        /**
         * Method to set the HTTP request hostname verifier when using an SSL URL
         *
         * @param hostnameVerifier
         * @return Builder
         */
        public Builder withHostnameVerifier(HostnameVerifier hostnameVerifier) {
            configuration.hostnameVerifier = hostnameVerifier;
            return this;
        }

        /**
         * Method to build the HttpRequest
         *
         * @return HttpRequest
         */
        public HttpRequest build() {
            return new HttpRequest(configuration);
        }
    }

    /**
     * Class used for Builder configuration
     */
    private static class Configuration {

        public METHOD method;
        public String url;
        public Map<String, List<String>> headers;
        public TrustManager[] trustManagers;
        public HostnameVerifier hostnameVerifier;

        /**
         * Constructor
         */
        Configuration() {
            method = METHOD.GET;
            headers = new HashMap<String, List<String>>();
        }

        /**
         * Method to add (append) an HTTP request header
         *
         * @param name
         * @param value
         * @return Configuration
         */
        void addHeader(String name, String value) {
            name = name.toLowerCase();
            List<String> values = headers.get(name);
            if (values == null) {
                values = new LinkedList<String>();
                headers.put(name, values);
            }

            values.add(value);
        }

        /**
         * Method to set (overwrite) an HTTP request header, removing all previous header values
         *
         * @param name
         * @param value
         * @return Configuration
         */
        void setHeader(String name, String value) {
            List<String> values = new LinkedList<String>();
            values.add(value);
            headers.put(name, values);
        }
    }

    /**
     * Method to encode "Authorization" credentials
     *
     * @param username
     * @param password
     * @return String
     */
    private final static String encodeCredentials(String username, String password) {
        // Per RFC4648 table 2. We support Java 6, and java.util.Base64 was only added in Java 8,
        try {
            byte[] credentialsBytes = (username + ":" + password).getBytes("UTF-8");
            return "Basic " + DatatypeConverter.printBase64Binary(credentialsBytes);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
