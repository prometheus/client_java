package io.prometheus.metrics.instrumentation.dropwizard.labels;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CustomLabelMapperTest {
    @Test(expected = IllegalArgumentException.class)
    public void test_WHEN_EmptyConfig_THEN_Fail() {
        final CustomLabelMapper converter = new CustomLabelMapper(Collections.<MapperConfig>emptyList());
    }

//    @Test
//    public void test_WHEN_NoMatches_THEN_ShouldReturnDefaultSample() {
//        final List<MapperConfig> mapperConfigs = Arrays.asList(
//                new MapperConfig("client-nope.*.*.*"),
//                new MapperConfig("*.client-nope.*.*.*"),
//                new MapperConfig("not.even.this.*.*.*")
//        );
//        final CustomMappingSampleBuilder converter = new CustomMappingSampleBuilder(mapperConfigs);
//        final Collector.MetricFamilySamples.Sample result = converter.createSample(
//                "app.okhttpclient.client.HttpClient.service.total", "", Collections.singletonList("quantile"), Collections.singletonList("0.99"), 0d);
//
//        assertEquals(new Collector.MetricFamilySamples.Sample("app_okhttpclient_client_HttpClient_service_total", Collections.singletonList("quantile"), Collections.singletonList("0.99"), 0d), result);
//    }
//
//    @Test
//    public void test_WHEN_OneMatch_THEN_ShouldReturnConverted() {
//        final Map<String, String> labels = new HashMap<String, String>();
//        labels.put("service", "${0}");
//        final MapperConfig mapperConfig = new MapperConfig(
//                "app.okhttpclient.client.HttpClient.*.total",
//                "app.okhttpclient.client.HttpClient.total",
//                labels
//        );
//        final List<MapperConfig> mapperConfigs = Arrays.asList(
//                new MapperConfig("client-nope.*.*.*"),
//                mapperConfig,
//                new MapperConfig("not.even.this.*.*.*")
//        );
//        final CustomMappingSampleBuilder converter = new CustomMappingSampleBuilder(mapperConfigs);
//        final Collector.MetricFamilySamples.Sample expectedResult = new Collector.MetricFamilySamples.Sample(
//                "app_okhttpclient_client_HttpClient_total", Collections.singletonList("service"), Collections.singletonList("greatService"), 1d);
//
//        final Collector.MetricFamilySamples.Sample result = converter.createSample(
//                "app.okhttpclient.client.HttpClient.greatService.total", "", Collections.<String>emptyList(), Collections.<String>emptyList(), 1d);
//
//        assertEquals(expectedResult, result);
//    }
//
//    @Test
//    public void test_WHEN_MoreMatches_THEN_ShouldReturnFirstOne() {
//        final Map<String, String> labels = new HashMap<String, String>();
//        labels.put("service", "${0}");
//        final MapperConfig mapperConfig = new MapperConfig(
//                "app.okhttpclient.client.HttpClient.*.total",
//                "app.okhttpclient.client.HttpClient.total",
//                labels
//        );
//        final List<MapperConfig> mapperConfigs = Arrays.asList(
//                new MapperConfig("client-nope.*.*.*"),
//                mapperConfig,
//                new MapperConfig("app.okhttpclient.client.HttpClient.*.*") // this matches as well
//        );
//        final CustomMappingSampleBuilder converter = new CustomMappingSampleBuilder(mapperConfigs);
//        final Collector.MetricFamilySamples.Sample expectedResult = new Collector.MetricFamilySamples.Sample(
//                "app_okhttpclient_client_HttpClient_total", Collections.singletonList("service"), Collections.singletonList("greatService"), 1d);
//
//        final Collector.MetricFamilySamples.Sample result = converter.createSample(
//                "app.okhttpclient.client.HttpClient.greatService.total", "", Collections.<String>emptyList(), Collections.<String>emptyList(), 1d);
//
//        assertEquals(expectedResult, result);
//    }
//
//    @Test
//    public void test_WHEN_MoreMatchesReverseOrder_THEN_ShouldReturnFirstOne() {
//        final Map<String, String> labels = new LinkedHashMap<String, String>();
//        labels.put("service", "${0}");
//        labels.put("status", "${1}");
//        final MapperConfig mapperConfig = new MapperConfig(
//                "app.okhttpclient.client.HttpClient.*.*",
//                "app.okhttpclient.client.HttpClient",
//                labels
//        );
//        final List<MapperConfig> mapperConfigs = Arrays.asList(
//                new MapperConfig("client-nope.*.*.*"),
//                mapperConfig,
//                new MapperConfig("app.okhttpclient.client.HttpClient.*.total") // this matches as well
//        );
//        final CustomMappingSampleBuilder converter = new CustomMappingSampleBuilder(mapperConfigs);
//        final Collector.MetricFamilySamples.Sample expectedResult = new Collector.MetricFamilySamples.Sample(
//                "app_okhttpclient_client_HttpClient", Arrays.asList("service", "status"), Arrays.asList("greatService", "400"), 1d);
//
//        final Collector.MetricFamilySamples.Sample result = converter.createSample(
//                "app.okhttpclient.client.HttpClient.greatService.400", "", Collections.<String>emptyList(), Collections.<String>emptyList(), 1d);
//
//        assertEquals(expectedResult, result);
//    }
//
//    @Test
//    public void test_WHEN_MoreToFormatInLabelsAndName_THEN_ShouldReturnCorrectSample() {
//        final Map<String, String> labels = new LinkedHashMap<String, String>();
//        labels.put("service", "${0}_${1}");
//        labels.put("status", "s_${1}");
//        final MapperConfig mapperConfig = new MapperConfig(
//                "app.okhttpclient.client.HttpClient.*.*",
//                "app.okhttpclient.client.HttpClient.${0}",
//                labels
//        );
//        final List<MapperConfig> mapperConfigs = Arrays.asList(
//                new MapperConfig("client-nope.*.*.*"),
//                mapperConfig,
//                new MapperConfig("app.okhttpclient.client.HttpClient.*.total") // this matches as well
//        );
//        final CustomMappingSampleBuilder converter = new CustomMappingSampleBuilder(mapperConfigs);
//        final Collector.MetricFamilySamples.Sample expectedResult = new Collector.MetricFamilySamples.Sample(
//                "app_okhttpclient_client_HttpClient_greatService", Arrays.asList("service", "status"), Arrays.asList("greatService_400", "s_400"), 1d);
//
//        final Collector.MetricFamilySamples.Sample result = converter.createSample(
//                "app.okhttpclient.client.HttpClient.greatService.400", "", Collections.<String>emptyList(), Collections.<String>emptyList(), 1d);
//
//        assertEquals(expectedResult, result);
//    }
//
//    @Test
//    public void test_WHEN_MetricNameSuffixRequested_THEN_ShouldReturnCorrectSample() {
//        final Map<String, String> labels = new LinkedHashMap<String, String>();
//        labels.put("service", "${0}");
//        labels.put("status", "s_${1}");
//        final MapperConfig mapperConfig = new MapperConfig(
//                "app.okhttpclient.client.HttpClient.*.*",
//                "app.okhttpclient.client.HttpClient.${0}",
//                labels
//        );
//        final List<MapperConfig> mapperConfigs = Arrays.asList(
//                new MapperConfig("client-nope.*.*.*"),
//                mapperConfig,
//                new MapperConfig("app.okhttpclient.client.HttpClient.*.total") // this matches as well
//        );
//        final CustomMappingSampleBuilder converter = new CustomMappingSampleBuilder(mapperConfigs);
//        final Collector.MetricFamilySamples.Sample expectedResult = new Collector.MetricFamilySamples.Sample(
//                "app_okhttpclient_client_HttpClient_greatService_suffix", Arrays.asList("service", "status"), Arrays.asList("greatService", "s_400"), 1d);
//
//        final Collector.MetricFamilySamples.Sample result = converter.createSample(
//                "app.okhttpclient.client.HttpClient.greatService.400", "_suffix", Collections.<String>emptyList(), Collections.<String>emptyList(), 1d);
//
//        assertEquals(expectedResult, result);
//    }
//
//    @Test
//    public void test_WHEN_AdditionalLabels_THEN_ShouldReturnCorrectSample() {
//        final Map<String, String> labels = new LinkedHashMap<String, String>();
//        labels.put("service", "${0}");
//        labels.put("status", "s_${1}");
//        final MapperConfig mapperConfig = new MapperConfig(
//                "app.okhttpclient.client.HttpClient.*.*",
//                "app.okhttpclient.client.HttpClient.${0}",
//                labels
//        );
//        final List<MapperConfig> mapperConfigs = Arrays.asList(
//                new MapperConfig("client-nope.*.*.*"),
//                mapperConfig,
//                new MapperConfig("app.okhttpclient.client.HttpClient.*.total") // this matches as well
//        );
//        final CustomMappingSampleBuilder converter = new CustomMappingSampleBuilder(mapperConfigs);
//        final Collector.MetricFamilySamples.Sample expectedResult = new Collector.MetricFamilySamples.Sample(
//                "app_okhttpclient_client_HttpClient_greatService_suffix", Arrays.asList("service", "status", "another"), Arrays.asList("greatService", "s_400", "label"), 1d);
//
//        final Collector.MetricFamilySamples.Sample result = converter.createSample(
//                "app.okhttpclient.client.HttpClient.greatService.400", "_suffix", Collections.singletonList("another"), Collections.singletonList("label"), 1d);
//
//        assertEquals(expectedResult, result);
//    }
}
