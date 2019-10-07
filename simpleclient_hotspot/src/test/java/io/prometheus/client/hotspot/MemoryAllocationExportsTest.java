package io.prometheus.client.hotspot;

import io.prometheus.client.Counter;
import org.junit.Test;
import org.mockito.Mockito;

import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import java.lang.management.GarbageCollectorMXBean;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MemoryAllocationExportsTest {

  @Test
  public void testListenerLogic() {
    Counter counter = Counter.build("test", "test").labelNames("pool").create();
    MemoryAllocationExports.AllocationCountingNotificationListener listener =
            new MemoryAllocationExports.AllocationCountingNotificationListener(counter);

    // Increase by 123
    listener.handleMemoryPool("TestPool", 0, 123);
    Counter.Child child = counter.labels("TestPool");
    assertEquals(123, child.get(), 0.001);

    // No increase
    listener.handleMemoryPool("TestPool", 123, 123);
    assertEquals(123, child.get(), 0.001);

    // No increase, then decrease to 0
    listener.handleMemoryPool("TestPool", 123, 0);
    assertEquals(123, child.get(), 0.001);

    // No increase, then increase by 7
    listener.handleMemoryPool("TestPool", 0, 7);
    assertEquals(130, child.get(), 0.001);

    // Increase by 10, then decrease to 10
    listener.handleMemoryPool("TestPool", 17, 10);
    assertEquals(140, child.get(), 0.001);

    // Increase by 7, then increase by 3
    listener.handleMemoryPool("TestPool", 17, 20);
    assertEquals(150, child.get(), 0.001);

    // Decrease to 17, then increase by 3
    listener.handleMemoryPool("TestPool", 17, 20);
    assertEquals(153, child.get(), 0.001);
  }

  @Test
  public void testConstructorShouldIgnoreNotNotificationEmitterClasses() {
    final GarbageCollectorMXBean notificationEmitterMXBean = Mockito.mock(GarbageCollectorMXBean.class, Mockito.withSettings().extraInterfaces(NotificationEmitter.class));
    final GarbageCollectorMXBean notNotificationEmitterMXBean = Mockito.mock(GarbageCollectorMXBean.class);

    new MemoryAllocationExports() {
      @Override
      protected List<GarbageCollectorMXBean> getGarbageCollectorMXBeans() {
          return Arrays.asList(notificationEmitterMXBean, notNotificationEmitterMXBean);
      }
    };

    Mockito.verify((NotificationEmitter) notificationEmitterMXBean).addNotificationListener(Mockito.any(MemoryAllocationExports.AllocationCountingNotificationListener.class), Mockito.isNull(NotificationFilter.class), Mockito.isNull());
    Mockito.verifyZeroInteractions(notNotificationEmitterMXBean);
  }
}
