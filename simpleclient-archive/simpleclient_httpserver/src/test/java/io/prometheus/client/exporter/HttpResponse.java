package io.prometheus.client.exporter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to perform HTTP testing
 */
class HttpResponse {

    private int responseCode;
    private Map<String, List<String>> headers;
    private long contentLength = -1;
    private String body;

    /**
     * Constructor
     *
     * @param responseCode
     * @param headers
     * @param contentLength
     * @param body
     */
    HttpResponse(int responseCode, Map<String, List<String>> headers, long contentLength, String body) throws IOException {
        this.responseCode = responseCode;
        this.body = body;
        this.contentLength = contentLength;
        this.headers = new HashMap<String, List<String>>();

        Set<Map.Entry<String, List<String>>> headerSet = headers.entrySet();
        for (String header : headers.keySet()) {
            if (header != null) {
                List<String> values = headers.get(header);
                this.headers.put(header.toLowerCase(), values);
            }
        }

        if (getHeader("content-length") != null && getHeader("transfer-encoding") != null) {
            throw new IOException("Invalid HTTP response, should only contain Connect-Length or Transfer-Encoding");
        }
    }

    /**
     * Method to get the HTTP response code
     *
     * @return int
     */
    public int getResponseCode() {
        return this.responseCode;
    }

    /**
     * Method to get a list of HTTP response headers values
     *
     * @param name
     * @return List<String>
     */
    public List<String> getHeaderList(String name) {
        return headers.get(name.toLowerCase());
    }

    /**
     * Method to get the first HTTP response header value
     *
     * @param name
     * @return String
     */
    public String getHeader(String name) {
        String value = null;

        List<String> valueList = getHeaderList(name);
        if (valueList != null && (valueList.size() >= 0)) {
            value = valueList.get(0);
        }

        return value;
    }

    /**
     * Method to get the first HTTP response header value as a Long.
     * Returns null of the header doesn't exist
     *
     * @param name
     * @return Long
     */
    public Long getHeaderAsLong(String name) {
        String value = getHeader(name);
        if (value != null) {
            try {
                return Long.valueOf(value);
            } catch (Exception e) {
            }
        }

        return null;
    }

    /**
     * Method to get the HTTP response body
     *
     * @return String
     */
    public String getBody() {
        return body;
    }
}
