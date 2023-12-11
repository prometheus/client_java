package io.prometheus.client.it.common;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Assert;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Scrape metrics via HTTP
 */
public class Scraper {

    /**
     * Scrape metrics from url without authentication.
     */
    public static List<String> scrape(String url, long timeoutMillis) {
        return scrape(url, null, null, timeoutMillis);
    }

    /**
     * Scrape metrics from url with HTTP basic authentication.
     */
    public static List<String> scrape(String url, String user, String password, long timeoutMillis) {
        OkHttpClient client = new OkHttpClient();
        long start = System.currentTimeMillis();
        Exception exception = null;
        while (System.currentTimeMillis() - start < timeoutMillis) {
            try {
                Request.Builder requestBuilder = new Request.Builder()
                        .header("Accept", "application/openmetrics-text; version=1.0.0; charset=utf-8")
                        .url(url);
                if (user != null && password != null) {
                    requestBuilder.header("Authorization", Credentials.basic(user, password));
                }
                Request request = requestBuilder.build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.code() != 200) {
                        throw new IOException("Received HTTP Status " + response.code() + ": " + response.body().string());
                    }
                    return Arrays.asList(response.body().string().split("\\n"));
                }
            } catch (Exception e) {
                exception = e;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }
        }
        if (exception != null) {
            exception.printStackTrace();
        }
        Assert.fail("timeout while getting metrics from " + url);
        return null; // will not happen
    }
}
