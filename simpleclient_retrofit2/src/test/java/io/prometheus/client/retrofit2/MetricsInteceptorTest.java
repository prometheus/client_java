package io.prometheus.client.retrofit2;

import io.prometheus.client.CollectorRegistry;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.*;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MetricsInteceptorTest {
    private RetrofitService service;

    @Before
    public void setup() throws IOException {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody("foo"));
        server.start();

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new MetricsInterceptor("retrofit_calls", null, null))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(server.url("/"))
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        service = retrofit.create(RetrofitService.class);
    }

    @After
    public void clear() {
        CollectorRegistry.defaultRegistry.clear();
    }

    @Test
    public void testHttpDeleteMethod() throws IOException {
        Response<String> response = service.deleteMethod("blah").execute();

        assertEquals("foo", response.body());
        assertMetric("delete/{param}", "DELETE");
    }

    @Test
    public void testHttpGetMethod() throws IOException {
        Response<String> response = service.getMethod("blah").execute();

        assertEquals("foo", response.body());
        assertMetric("get/{param}", "GET");
    }

    @Test
    public void testHttpHeadMethod() throws IOException {
        service.headMethod("blah").execute();

        assertMetric("head/{param}", "HEAD");
    }

    @Test
    public void testHttpPatchMethod() throws IOException {
        Response<String> response = service.patchMethod("blah").execute();

        assertEquals("foo", response.body());
        assertMetric("patch/{param}", "PATCH");
    }

    @Test
    public void testHttpPostMethod() throws IOException {
        Response<String> response = service.postMethod("blah").execute();

        assertEquals("foo", response.body());
        assertMetric("post/{param}", "POST");
    }

    @Test
    public void testHttpPutMethod() throws IOException {
        Response<String> response = service.putMethod("blah").execute();

        assertEquals("foo", response.body());
        assertMetric("put/{param}", "PUT");
    }

    @Test
    public void testHttpOptionsMethod() throws IOException {
        Response<String> response = service.optionsMethod("blah").execute();

        assertEquals("foo", response.body());
        assertMetric("options/{param}", "OPTIONS");
    }

    @Test
    public void testHttpMethod() throws IOException {
        Response<String> response = service.httpMethod("blah").execute();

        assertEquals("foo", response.body());
        assertMetric("http/{param}", "GET");
    }

    private void assertMetric(String path, String method) {
        Double sampleValue = CollectorRegistry.defaultRegistry.getSampleValue("retrofit_calls_count", new String[]{"path", "method"}, new String[]{path, method});
        assertNotNull("Metric not found", sampleValue);
        assertEquals(1, sampleValue, 0.0001);
    }

    private interface RetrofitService {
        @DELETE("delete/{param}")
        Call<String> deleteMethod(@Path("param") String param);

        @GET("get/{param}")
        Call<String> getMethod(@Path("param") String param);

        @HEAD("head/{param}")
        Call<Void> headMethod(@Path("param") String param);

        @PATCH("patch/{param}")
        Call<String> patchMethod(@Path("param") String param);

        @POST("post/{param}")
        Call<String> postMethod(@Path("param") String param);

        @PUT("put/{param}")
        Call<String> putMethod(@Path("param") String param);

        @OPTIONS("options/{param}")
        Call<String> optionsMethod(@Path("param") String param);

        @HTTP(method = "GET", path = "http/{param}")
        Call<String> httpMethod(@Path("param") String param);
    }
}
