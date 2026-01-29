package io.prometheus.metrics.instrumentation.jvm;

import static com.sun.management.GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION;
import static io.prometheus.metrics.instrumentation.jvm.TestUtil.convertToOpenMetricsFormat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.prometheus.metrics.config.MetricsProperties;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.registry.MetricNameFilter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class JvmGarbageCollectorMetricsTest {

  private final GarbageCollectorMXBean mockGcBean1 = mock(GarbageCollectorMXBean.class);
  private final GarbageCollectorMXBean mockGcBean2 = mock(GarbageCollectorMXBean.class);

  @BeforeEach
  public void setUp() {
    when(mockGcBean1.getName()).thenReturn("MyGC1");
    when(mockGcBean1.getCollectionCount()).thenReturn(100L);
    when(mockGcBean1.getCollectionTime()).thenReturn(TimeUnit.SECONDS.toMillis(10));
    when(mockGcBean2.getName()).thenReturn("MyGC2");
    when(mockGcBean2.getCollectionCount()).thenReturn(200L);
    when(mockGcBean2.getCollectionTime()).thenReturn(TimeUnit.SECONDS.toMillis(20));
  }

  @Test
  public void testGoodCase() throws IOException {
    PrometheusRegistry registry = new PrometheusRegistry();
    JvmGarbageCollectorMetrics.builder()
        .garbageCollectorBeans(Arrays.asList(mockGcBean1, mockGcBean2))
        .register(registry);
    MetricSnapshots snapshots = registry.scrape();

    String expected =
        """
        # TYPE jvm_gc_collection_seconds summary
        # UNIT jvm_gc_collection_seconds seconds
        # HELP jvm_gc_collection_seconds Time spent in a given JVM garbage collector in seconds.
        jvm_gc_collection_seconds_count{gc="MyGC1"} 100
        jvm_gc_collection_seconds_sum{gc="MyGC1"} 10.0
        jvm_gc_collection_seconds_count{gc="MyGC2"} 200
        jvm_gc_collection_seconds_sum{gc="MyGC2"} 20.0
        # EOF
        """;

    assertThat(convertToOpenMetricsFormat(snapshots)).isEqualTo(expected);
  }

  @Test
  public void testIgnoredMetricNotScraped() {
    MetricNameFilter filter =
        MetricNameFilter.builder()
            .nameMustNotBeEqualTo("jvm_gc_collection_seconds", "jvm_gc_duration")
            .build();

    PrometheusRegistry registry = new PrometheusRegistry();
    JvmGarbageCollectorMetrics.builder()
        .garbageCollectorBeans(Arrays.asList(mockGcBean1, mockGcBean2))
        .register(registry);
    MetricSnapshots snapshots = registry.scrape(filter);

    verify(mockGcBean1, times(0)).getCollectionTime();
    verify(mockGcBean1, times(0)).getCollectionCount();
    assertThat(snapshots.size()).isZero();
  }

  @Test
  public void testNonOtelMetricsAbsentWhenUseOtelEnabled() {

    PrometheusRegistry registry = new PrometheusRegistry();
    PrometheusProperties properties =
        PrometheusProperties.builder()
            .defaultMetricsProperties(MetricsProperties.builder().useOtelMetrics(true).build())
            .build();
    JvmGarbageCollectorMetrics.builder(properties)
        .garbageCollectorBeans(Arrays.asList(mockGcBean1, mockGcBean2))
        .register(registry);
    registry.scrape();

    verify(mockGcBean1, times(0)).getCollectionTime();
    verify(mockGcBean1, times(0)).getCollectionCount();
  }

  @Test
  @SuppressWarnings("rawtypes")
  public void testGCDurationHistogramLabels() throws Exception {
    GarbageCollectorMXBean mockGcBean =
        mock(
            GarbageCollectorMXBean.class,
            withSettings().extraInterfaces(NotificationEmitter.class));
    when(mockGcBean.getName()).thenReturn("MyGC");

    PrometheusProperties properties =
        PrometheusProperties.builder()
            .defaultMetricsProperties(MetricsProperties.builder().useOtelMetrics(true).build())
            .build();

    PrometheusRegistry registry = new PrometheusRegistry();
    JvmGarbageCollectorMetrics.builder(properties)
        .garbageCollectorBeans(Collections.singletonList(mockGcBean))
        .register(registry);

    NotificationListener listener;
    ArgumentCaptor<NotificationListener> captor = forClass(NotificationListener.class);
    verify((NotificationEmitter) mockGcBean)
        .addNotificationListener(captor.capture(), isNull(), isNull());
    listener = captor.getValue();

    TabularType memoryTabularType = getMemoryTabularType();
    TabularData memoryBefore = new TabularDataSupport(memoryTabularType);
    TabularData memoryAfter = new TabularDataSupport(memoryTabularType);

    CompositeType gcInfoType =
        new CompositeType(
            "sun.management.BaseGcInfoCompositeType",
            "gcInfo",
            new String[] {
              "id", "startTime", "endTime", "duration", "memoryUsageBeforeGc", "memoryUsageAfterGc"
            },
            new String[] {
              "id", "startTime", "endTime", "duration", "memoryUsageBeforeGc", "memoryUsageAfterGc"
            },
            new OpenType<?>[] {
              SimpleType.LONG,
              SimpleType.LONG,
              SimpleType.LONG,
              SimpleType.LONG,
              memoryTabularType,
              memoryTabularType
            });

    java.util.Map<String, Object> gcInfoMap = new HashMap<>();
    gcInfoMap.put("id", 0L);
    gcInfoMap.put("startTime", 100L);
    gcInfoMap.put("endTime", 200L);
    gcInfoMap.put("duration", 100L);
    gcInfoMap.put("memoryUsageBeforeGc", memoryBefore);
    gcInfoMap.put("memoryUsageAfterGc", memoryAfter);

    CompositeData notificationData = getGcNotificationData(gcInfoType, gcInfoMap);

    Notification notification =
        new Notification(
            GARBAGE_COLLECTION_NOTIFICATION, mockGcBean, 1, System.currentTimeMillis(), "gc");
    notification.setUserData(notificationData);

    listener.handleNotification(notification, null);

    MetricSnapshots snapshots = registry.scrape();

    String expected =
        """
    {"jvm.gc.duration_bucket","jvm.gc.action"="end of minor GC","jvm.gc.cause"="testCause","jvm.gc.name"="MyGC",le="0.01"} 0
    {"jvm.gc.duration_bucket","jvm.gc.action"="end of minor GC","jvm.gc.cause"="testCause","jvm.gc.name"="MyGC",le="0.1"} 1
    {"jvm.gc.duration_bucket","jvm.gc.action"="end of minor GC","jvm.gc.cause"="testCause","jvm.gc.name"="MyGC",le="1.0"} 1
    {"jvm.gc.duration_bucket","jvm.gc.action"="end of minor GC","jvm.gc.cause"="testCause","jvm.gc.name"="MyGC",le="10.0"} 1
    {"jvm.gc.duration_bucket","jvm.gc.action"="end of minor GC","jvm.gc.cause"="testCause","jvm.gc.name"="MyGC",le="+Inf"} 1
    {"jvm.gc.duration_count","jvm.gc.action"="end of minor GC","jvm.gc.cause"="testCause","jvm.gc.name"="MyGC"} 1
    {"jvm.gc.duration_sum","jvm.gc.action"="end of minor GC","jvm.gc.cause"="testCause","jvm.gc.name"="MyGC"} 0.1
    """;

    String metrics = convertToOpenMetricsFormat(snapshots);

    assertThat(metrics).contains(expected);
  }

  private TabularType getMemoryTabularType() throws OpenDataException {
    CompositeType memoryUsageType =
        new CompositeType(
            "java.lang.management.MemoryUsage",
            "MemoryUsage",
            new String[] {"init", "used", "committed", "max"},
            new String[] {"init", "used", "committed", "max"},
            new OpenType<?>[] {SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG});

    CompositeType memoryUsageEntryType =
        new CompositeType(
            "memoryUsageEntry",
            "memoryUsageEntry",
            new String[] {"key", "value"},
            new String[] {"key", "value"},
            new OpenType<?>[] {SimpleType.STRING, memoryUsageType});

    return new TabularType(
        "memoryUsageTabular", "memoryUsageTabular", memoryUsageEntryType, new String[] {"key"});
  }

  private static CompositeData getGcNotificationData(
      CompositeType gcInfoType, Map<String, Object> gcInfoMap) throws OpenDataException {
    CompositeData gcInfoData = new CompositeDataSupport(gcInfoType, gcInfoMap);

    CompositeType notificationType =
        new CompositeType(
            "sun.management.BaseGarbageCollectionNotifInfoCompositeType",
            "GarbageCollectionNotificationInfo",
            new String[] {"gcAction", "gcName", "gcCause", "gcInfo"},
            new String[] {"gcAction", "gcName", "gcCause", "gcInfo"},
            new OpenType[] {SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, gcInfoType});

    Map<String, Object> notifMap = new HashMap<>();
    notifMap.put("gcAction", "end of minor GC");
    notifMap.put("gcName", "MyGC");
    notifMap.put("gcCause", "testCause");
    notifMap.put("gcInfo", gcInfoData);

    return new CompositeDataSupport(notificationType, notifMap);
  }
}
