package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;

import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Exports metrics about JVM buffers.
 *
 * Can be replaced with a simple access once JDK 1.7 compatibility is baseline.
 *
 */
public class BufferPoolsExports extends Collector {

    private static final Logger LOGGER = Logger.getLogger(BufferPoolsExports.class.getName());

    private final List<Object> bufferPoolMXBeans = new ArrayList<Object>();
    private Method getName;
    private Method getMemoryUsed;
    private Method getTotalCapacity;
    private Method getCount;

    public BufferPoolsExports() {
        try {
            final Class<?> bufferPoolMXBeanClass = Class.forName("java.lang.management.BufferPoolMXBean");
            bufferPoolMXBeans.addAll(accessBufferPoolMXBeans(bufferPoolMXBeanClass));

            getName = bufferPoolMXBeanClass.getMethod("getName");
            getMemoryUsed = bufferPoolMXBeanClass.getMethod("getMemoryUsed");
            getTotalCapacity = bufferPoolMXBeanClass.getMethod("getTotalCapacity");
            getCount = bufferPoolMXBeanClass.getMethod("getCount");

        } catch (ClassNotFoundException e) {
            LOGGER.fine("BufferPoolMXBean not available, no metrics for buffer pools will be exported");
        } catch (NoSuchMethodException e) {
            LOGGER.fine("Can not get necessary accessor from BufferPoolMXBean: " + e.getMessage());
        }
    }

    private static List<Object> accessBufferPoolMXBeans(final Class<?> bufferPoolMXBeanClass) {
        try {
            final Method getPlatformMXBeansMethod = ManagementFactory.class.getMethod("getPlatformMXBeans", Class.class);
            final Object listOfBufferPoolMXBeanInstances = getPlatformMXBeansMethod.invoke(null, bufferPoolMXBeanClass);

            return (List<Object>) listOfBufferPoolMXBeanInstances;

        } catch (NoSuchMethodException e) {
            LOGGER.fine("ManagementFactory.getPlatformMXBeans not available, no metrics for buffer pools will be exported");
            return Collections.emptyList();
        } catch (IllegalAccessException e) {
            LOGGER.fine("ManagementFactory.getPlatformMXBeans not accessible, no metrics for buffer pools will be exported");
            return Collections.emptyList();
        } catch (InvocationTargetException e) {
            LOGGER.warning("ManagementFactory.getPlatformMXBeans could not be invoked, no metrics for buffer pools will be exported");
            return Collections.emptyList();
        }
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
        GaugeMetricFamily used = new GaugeMetricFamily(
                "jvm_buffer_pool_used_bytes",
                "Used bytes of a given JVM buffer pool.",
                Collections.singletonList("pool"));
        mfs.add(used);
        GaugeMetricFamily capacity = new GaugeMetricFamily(
                "jvm_buffer_pool_capacity_bytes",
                "Bytes capacity of a given JVM buffer pool.",
                Collections.singletonList("pool"));
        mfs.add(capacity);
        GaugeMetricFamily buffers = new GaugeMetricFamily(
                "jvm_buffer_pool_used_buffers",
                "Used buffers of a given JVM buffer pool.",
                Collections.singletonList("pool"));
        mfs.add(buffers);
        for (final Object pool : bufferPoolMXBeans) {
            used.addMetric(
                    Collections.singletonList(getName(pool)),
                    callLongMethond(getMemoryUsed,pool));
            capacity.addMetric(
                    Collections.singletonList(getName(pool)),
                    callLongMethond(getTotalCapacity,pool));
            buffers.addMetric(
                    Collections.singletonList(getName(pool)),
                    callLongMethond(getCount,pool));
        }
        return mfs;
    }

    private long callLongMethond(final Method method, final Object pool) {
        try {
            return (Long)method.invoke(pool);
        } catch (IllegalAccessException e) {
            LOGGER.fine("Couldn't call " + method.getName() + ": " + e.getMessage());
        } catch (InvocationTargetException e) {
            LOGGER.fine("Couldn't call " + method.getName() + ": " + e.getMessage());
        }
        return 0L;
    }

    private String getName(final Object pool) {
        try {
            return (String)getName.invoke(pool);
        } catch (IllegalAccessException e) {
            LOGGER.fine("Couldn't call getName " + e.getMessage());
        } catch (InvocationTargetException e) {
            LOGGER.fine("Couldn't call getName " + e.getMessage());
        }
        return "<unknown>";
    }



}
