package io.prometheus.client.retrofit2;

import io.prometheus.client.Histogram;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Invocation;
import retrofit2.http.*;

import java.io.IOException;
import java.lang.annotation.Annotation;

/**
 * Collect metrics from Retrofit when using OkHttp. Metrics will include path and HTTP method.
 *
 * <p>Modeled after {@code io.prometheus.client.filter.MetricsFilter}
 *
 * <p>
 * Usage example:
 * <pre>
 * OkHttpClient client = new OkHttpClient.Builder()
 *     .addInterceptor(new MetricsInterceptor("retrofit_calls", null, null))
 *     .build();
 *
 *  Retrofit retrofit = new Retrofit.Builder()
 *     .baseUrl("http://acme.com")
 *     .client(client)
 *     .build();
 * </pre>
 *
 * Requires a minimum of Retrofit 2.5.0 and OkHttp 3.11
 * <p>
 */
public class MetricsInterceptor implements Interceptor {
    private final Histogram histogram;

    public MetricsInterceptor(String metricName, String help, double[] buckets) {
        Histogram.Builder builder = Histogram.build().name(metricName).labelNames("path", "method");

        if (help == null) {
            help = "The time taken fulfilling retrofit requests";
        }
        builder.help(help);

        if (buckets != null) {
            builder.buckets(buckets);
        }

        this.histogram = builder.register();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String method = "";
        String path = "";

        Invocation invocation = request.tag(Invocation.class);
        if (invocation != null) {
            for (Annotation annotation : invocation.method().getAnnotations()) {
                if (annotation instanceof DELETE) {
                    method = "DELETE";
                    path = ((DELETE) annotation).value();
                } else if (annotation instanceof GET) {
                    method = "GET";
                    path = ((GET) annotation).value();
                } else if (annotation instanceof HEAD) {
                    method = "HEAD";
                    path = ((HEAD) annotation).value();
                } else if (annotation instanceof PATCH) {
                    method = "PATCH";
                    path = ((PATCH) annotation).value();
                } else if (annotation instanceof POST) {
                    method = "POST";
                    path = ((POST) annotation).value();
                } else if (annotation instanceof PUT) {
                    method = "PUT";
                    path = ((PUT) annotation).value();
                } else if (annotation instanceof OPTIONS) {
                    method = "OPTIONS";
                    path = ((OPTIONS) annotation).value();
                } else if (annotation instanceof HTTP) {
                    method = ((HTTP) annotation).method();
                    path = ((HTTP) annotation).path();
                }
            }
        }

        Histogram.Timer timer = histogram
                .labels(path, method)
                .startTimer();

        try {
            return chain.proceed(request);
        } finally {
            timer.observeDuration();
        }
    }
}
