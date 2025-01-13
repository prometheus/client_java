package io.prometheus.metrics.instrumentation.dropwizard5.labels;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.nameEscapingScheme;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import io.dropwizard.metrics5.MetricFilter;
import io.dropwizard.metrics5.MetricRegistry;
import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.instrumentation.dropwizard5.DropwizardExports;
import io.prometheus.metrics.model.snapshots.EscapingScheme;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomLabelMapperTest {
  private MetricRegistry metricRegistry;

  @BeforeEach
  public void setUp() {
    metricRegistry = new MetricRegistry();
  }

  @Test
  public void test_WHEN_EmptyConfig_THEN_Fail() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> new CustomLabelMapper(Collections.emptyList()));
  }

  @Test
  public void test_WHEN_NoMatches_THEN_ShouldReturnDefaultSample() {
    final List<MapperConfig> mapperConfigs =
        Arrays.asList(
            new MapperConfig("client-nope.*.*.*"),
            new MapperConfig("*.client-nope.*.*.*"),
            new MapperConfig("not.even.this.*.*.*"));
    final CustomLabelMapper labelMapper = new CustomLabelMapper(mapperConfigs);
    DropwizardExports dropwizardExports =
        new DropwizardExports(metricRegistry, MetricFilter.ALL, labelMapper);

    metricRegistry.counter("app.okhttpclient.client.HttpClient.service.total").inc(1);
    System.out.println(convertToOpenMetricsFormat(dropwizardExports.collect()));

    String expected =
        "# TYPE app_okhttpclient_client_HttpClient_service counter\n"
            + "# HELP app_okhttpclient_client_HttpClient_service Generated from Dropwizard metric import (metric=app.okhttpclient.client.HttpClient.service.total, type=io.dropwizard.metrics5.Counter)\n"
            + "app_okhttpclient_client_HttpClient_service_total 1.0\n"
            + "# EOF\n";

    assertThat(convertToOpenMetricsFormat(dropwizardExports.collect())).isEqualTo(expected);
  }

  @Test
  public void test_WHEN_OneMatch_THEN_ShouldReturnConverted() {
    final Map<String, String> labels = new HashMap<String, String>();
    labels.put("service", "${0}");
    final MapperConfig mapperConfig =
        new MapperConfig(
            "app.okhttpclient.client.HttpClient.*.total",
            "app.okhttpclient.client.HttpClient.total",
            labels);
    final List<MapperConfig> mapperConfigs =
        Arrays.asList(
            new MapperConfig("client-nope.*.*.*"),
            mapperConfig,
            new MapperConfig("not.even.this.*.*.*"));
    final CustomLabelMapper labelMapper = new CustomLabelMapper(mapperConfigs);
    DropwizardExports dropwizardExports =
        new DropwizardExports(metricRegistry, MetricFilter.ALL, labelMapper);

    metricRegistry.counter("app.okhttpclient.client.HttpClient.greatService.total").inc(1);

    String expected =
        "# TYPE app_okhttpclient_client_HttpClient counter\n"
            + "# HELP app_okhttpclient_client_HttpClient Generated from Dropwizard metric import (metric=app.okhttpclient.client.HttpClient.greatService.total, type=io.dropwizard.metrics5.Counter)\n"
            + "app_okhttpclient_client_HttpClient_total{service=\"greatService\"} 1.0\n"
            + "# EOF\n";
    assertThat(convertToOpenMetricsFormat(dropwizardExports.collect())).isEqualTo(expected);
  }

  @Test
  public void test_WHEN_MoreMatches_THEN_ShouldReturnFirstOne() {
    final Map<String, String> labels = new HashMap<>();
    labels.put("service", "${0}");
    final MapperConfig mapperConfig =
        new MapperConfig(
            "app.okhttpclient.client.HttpClient.*.total",
            "app.okhttpclient.client.HttpClient.total",
            labels);
    final List<MapperConfig> mapperConfigs =
        Arrays.asList(
            new MapperConfig("client-nope.*.*.*"),
            mapperConfig,
            new MapperConfig("app.okhttpclient.client.HttpClient.*.*") // this matches as well
            );
    final CustomLabelMapper labelMapper = new CustomLabelMapper(mapperConfigs);
    DropwizardExports dropwizardExports =
        new DropwizardExports(metricRegistry, MetricFilter.ALL, labelMapper);

    metricRegistry.counter("app.okhttpclient.client.HttpClient.greatService.total").inc(1);

    String expected =
        "# TYPE app_okhttpclient_client_HttpClient counter\n"
            + "# HELP app_okhttpclient_client_HttpClient Generated from Dropwizard metric import (metric=app.okhttpclient.client.HttpClient.greatService.total, type=io.dropwizard.metrics5.Counter)\n"
            + "app_okhttpclient_client_HttpClient_total{service=\"greatService\"} 1.0\n"
            + "# EOF\n";
    assertThat(convertToOpenMetricsFormat(dropwizardExports.collect())).isEqualTo(expected);
  }

  @Test
  public void test_WHEN_MoreMatchesReverseOrder_THEN_ShouldReturnFirstOne() {
    final Map<String, String> labels = new LinkedHashMap<>();
    labels.put("service", "${0}");
    labels.put("status", "${1}");
    final MapperConfig mapperConfig =
        new MapperConfig(
            "app.okhttpclient.client.HttpClient.*.*", "app.okhttpclient.client.HttpClient", labels);

    final MapperConfig mapperConfig2 =
        new MapperConfig(
            "app.okhttpclient.client.HttpClient.*.*",
            "app.okhttpclient.client.HttpClient2",
            labels);

    final List<MapperConfig> mapperConfigs =
        Arrays.asList(
            new MapperConfig("client-nope.*.*.*"),
            mapperConfig,
            mapperConfig2 // this matches as well
            );

    final CustomLabelMapper labelMapper = new CustomLabelMapper(mapperConfigs);
    DropwizardExports dropwizardExports =
        new DropwizardExports(metricRegistry, MetricFilter.ALL, labelMapper);
    metricRegistry.counter("app.okhttpclient.client.HttpClient.greatService.400").inc(1);

    String expected =
        "# TYPE app_okhttpclient_client_HttpClient counter\n"
            + "# HELP app_okhttpclient_client_HttpClient Generated from Dropwizard metric import (metric=app.okhttpclient.client.HttpClient.greatService.400, type=io.dropwizard.metrics5.Counter)\n"
            + "app_okhttpclient_client_HttpClient_total{service=\"greatService\",status=\"400\"} 1.0\n"
            + "# EOF\n";
    assertThat(convertToOpenMetricsFormat(dropwizardExports.collect())).isEqualTo(expected);
  }

  @Test
  public void test_WHEN_MoreToFormatInLabelsAndName_THEN_ShouldReturnCorrectSample() {
    final Map<String, String> labels = new LinkedHashMap<>();
    labels.put("service", "${0}_${1}");
    labels.put("status", "s_${1}");
    final MapperConfig mapperConfig =
        new MapperConfig(
            "app.okhttpclient.client.HttpClient.*.*",
            "app.okhttpclient.client.HttpClient.${0}",
            labels);
    final List<MapperConfig> mapperConfigs =
        Arrays.asList(
            new MapperConfig("client-nope.*.*.*"),
            mapperConfig,
            new MapperConfig("app.okhttpclient.client.HttpClient.*.*") // this matches as well
            );

    final CustomLabelMapper labelMapper = new CustomLabelMapper(mapperConfigs);
    DropwizardExports dropwizardExports =
        new DropwizardExports(metricRegistry, MetricFilter.ALL, labelMapper);
    metricRegistry.counter("app.okhttpclient.client.HttpClient.greatService.400").inc(1);
    System.out.println(convertToOpenMetricsFormat(dropwizardExports.collect()));

    String expected =
        "# TYPE app_okhttpclient_client_HttpClient_greatService counter\n"
            + "# HELP app_okhttpclient_client_HttpClient_greatService Generated from Dropwizard metric import (metric=app.okhttpclient.client.HttpClient.greatService.400, type=io.dropwizard.metrics5.Counter)\n"
            + "app_okhttpclient_client_HttpClient_greatService_total{service=\"greatService_400\",status=\"s_400\"} 1.0\n"
            + "# EOF\n";
    assertThat(convertToOpenMetricsFormat(dropwizardExports.collect())).isEqualTo(expected);
  }

  @Test
  public void test_WHEN_AdditionalLabels_THEN_ShouldReturnCorrectSample() {
    final Map<String, String> labels = new LinkedHashMap<>();
    labels.put("service", "${0}");
    labels.put("status", "s_${1}");
    labels.put("client", "sampleClient");
    final MapperConfig mapperConfig =
        new MapperConfig(
            "app.okhttpclient.client.HttpClient.*.*",
            "app.okhttpclient.client.HttpClient.${0}",
            labels);
    final List<MapperConfig> mapperConfigs =
        Arrays.asList(new MapperConfig("client-nope.*.*.*"), mapperConfig);

    final CustomLabelMapper labelMapper = new CustomLabelMapper(mapperConfigs);
    DropwizardExports dropwizardExports =
        new DropwizardExports(metricRegistry, MetricFilter.ALL, labelMapper);
    metricRegistry.counter("app.okhttpclient.client.HttpClient.greatService.400").inc(1);

    String expected =
        "# TYPE app_okhttpclient_client_HttpClient_greatService counter\n"
            + "# HELP app_okhttpclient_client_HttpClient_greatService Generated from Dropwizard metric import (metric=app.okhttpclient.client.HttpClient.greatService.400, type=io.dropwizard.metrics5.Counter)\n"
            + "app_okhttpclient_client_HttpClient_greatService_total{client=\"sampleClient\",service=\"greatService\",status=\"s_400\"} 1.0\n"
            + "# EOF\n";
    assertThat(convertToOpenMetricsFormat(dropwizardExports.collect())).isEqualTo(expected);
  }

  private String convertToOpenMetricsFormat(MetricSnapshots snapshots) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    OpenMetricsTextFormatWriter writer = new OpenMetricsTextFormatWriter(true, true);
    try {
      nameEscapingScheme = EscapingScheme.NO_ESCAPING;
      writer.write(out, snapshots);
      return out.toString(StandardCharsets.UTF_8.name());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
