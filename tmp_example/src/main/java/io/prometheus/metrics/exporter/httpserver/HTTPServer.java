package io.prometheus.metrics.exporter.httpserver;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import io.prometheus.metrics.config.HttpServerProperties;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.expositionformats.ExpositionFormatWriter;
import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.expositionformats.PrometheusProtobufWriter;
import io.prometheus.metrics.expositionformats.PrometheusTextFormatWriter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * Expose Prometheus metrics using a plain Java HttpServer.
 * <p>
 * Example Usage:
 * <pre>
 * {@code
 * HTTPServer server = new HTTPServer(1234);
 * }
 * </pre>
 * */
public class HTTPServer implements Closeable {

    static {
        if (!System.getProperties().containsKey("sun.net.httpserver.maxReqTime")) {
            System.setProperty("sun.net.httpserver.maxReqTime", "60");
        }

        if (!System.getProperties().containsKey("sun.net.httpserver.maxRspTime")) {
            System.setProperty("sun.net.httpserver.maxRspTime", "600");
        }
    }

    static class NamedDaemonThreadFactory implements ThreadFactory {
        private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);

        private final int poolNumber = POOL_NUMBER.getAndIncrement();
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadFactory delegate;
        private final boolean daemon;

        NamedDaemonThreadFactory(ThreadFactory delegate, boolean daemon) {
            this.delegate = delegate;
            this.daemon = daemon;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = delegate.newThread(r);
            t.setName(String.format("prometheus-http-%d-%d", poolNumber, threadNumber.getAndIncrement()));
            t.setDaemon(daemon);
            return t;
        }

        static ThreadFactory defaultThreadFactory(boolean daemon) {
            return new NamedDaemonThreadFactory(Executors.defaultThreadFactory(), daemon);
        }
    }

    protected final HttpServer server;
    protected final ExecutorService executorService;

    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * We keep the original constructors of {@link HTTPServer} for compatibility, but new configuration
     * parameters like {@code sampleNameFilter} must be configured using the Builder.
     */
    public static class Builder {

        private Integer port = null;
        private String hostname = null;
        private InetAddress inetAddress = null;
        private InetSocketAddress inetSocketAddress = null;
        private HttpServer httpServer = null;
        private ExecutorService executorService = null;
        private PrometheusRegistry registry = null;
        private HttpServerProperties properties = null;
        private boolean daemon = false;
        private Predicate<String> sampleNameFilter;
        private Authenticator authenticator = null;
        private HttpsConfigurator httpsConfigurator = null;

        private Builder() {}

        /**
         * Port to bind to. Must not be called together with {@link #withInetSocketAddress(InetSocketAddress)}
         * or {@link #withHttpServer(HttpServer)}. Default is 0, indicating that a random port will be selected.
         */
        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public Builder withProperties(HttpServerProperties properties) {
            this.properties = properties;
            return this;
        }

        /**
         * Use this hostname to resolve the IP address to bind to. Must not be called together with
         * {@link #withInetAddress(InetAddress)} or {@link #withInetSocketAddress(InetSocketAddress)}
         * or {@link #withHttpServer(HttpServer)}.
         * Default is empty, indicating that the HTTPServer binds to the wildcard address.
         */
        public Builder withHostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        /**
         * Bind to this IP address. Must not be called together with {@link #withHostname(String)} or
         * {@link #withInetSocketAddress(InetSocketAddress)} or {@link #withHttpServer(HttpServer)}.
         * Default is empty, indicating that the HTTPServer binds to the wildcard address.
         */
        public Builder withInetAddress(InetAddress address) {
            this.inetAddress = address;
            return this;
        }

        /**
         * Listen on this address. Must not be called together with {@link #withPort(int)},
         * {@link #withHostname(String)}, {@link #withInetAddress(InetAddress)}, or {@link #withHttpServer(HttpServer)}.
         */
        public Builder withInetSocketAddress(InetSocketAddress address) {
            this.inetSocketAddress = address;
            return this;
        }

        /**
         * Use this httpServer. The {@code httpServer} is expected to already be bound to an address.
         * Must not be called together with {@link #withPort(int)}, or {@link #withHostname(String)},
         * or {@link #withInetAddress(InetAddress)}, or {@link #withInetSocketAddress(InetSocketAddress)},
         * or {@link #withExecutorService(ExecutorService)}.
         */
        public Builder withHttpServer(HttpServer httpServer) {
            this.httpServer = httpServer;
            return this;
        }

        /**
         * Optional: ExecutorService used by the {@code httpServer}.
         * Must not be called together with the {@link #withHttpServer(HttpServer)}.
         *
         * @param executorService
         * @return
         */
        public Builder withExecutorService(ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

        /**
         * By default, the {@link HTTPServer} uses non-daemon threads. Set this to {@code true} to
         * run the {@link HTTPServer} with daemon threads.
         */
        public Builder withDaemonThreads(boolean daemon) {
            this.daemon = daemon;
            return this;
        }

        /**
         * Optional: Only export time series where {@code sampleNameFilter.test(name)} returns true.
         */
        public Builder withSampleNameFilter(Predicate<String> sampleNameFilter) {
            this.sampleNameFilter = sampleNameFilter;
            return this;
        }

        /**
         * Optional: Default is {@link PrometheusRegistry#defaultRegistry}.
         */
        public Builder withRegistry(PrometheusRegistry registry) {
            this.registry = registry;
            return this;
        }

        /**
         * Optional: {@link Authenticator} to use to support authentication.
         */
        public Builder withAuthenticator(Authenticator authenticator) {
            this.authenticator = authenticator;
            return this;
        }

        /**
         * Optional: {@link HttpsConfigurator} to use to support TLS/SSL
         */
        public Builder withHttpsConfigurator(HttpsConfigurator configurator) {
            this.httpsConfigurator = configurator;
            return this;
        }

        private int findPort() {
            if (properties != null && properties.getPort() != null) {
                return properties.getPort();
            }
            if (port != null) {
                return port;
            }
            HttpServerProperties defaultProperties = PrometheusProperties.get().getHttpServerConfig();
            if (defaultProperties != null && defaultProperties.getPort() != null) {
                return defaultProperties.getPort();
            }
            return 0; // random port will be selected
        }

        /**
         * Build the HTTPServer
         * @throws IOException
         */
        public HTTPServer build() throws IOException {
            if (registry == null) {
                registry = PrometheusRegistry.defaultRegistry;
            }

            if (httpServer != null) {
                assertNull(executorService, "cannot configure 'httpServer' and `executorService'");
                assertZero(port, "cannot configure 'httpServer' and 'port' at the same time");
                assertNull(hostname, "cannot configure 'httpServer' and 'hostname' at the same time");
                assertNull(inetAddress, "cannot configure 'httpServer' and 'inetAddress' at the same time");
                assertNull(inetSocketAddress, "cannot configure 'httpServer' and 'inetSocketAddress' at the same time");
                assertNull(httpsConfigurator, "cannot configure 'httpServer' and 'httpsConfigurator' at the same time");
                return new HTTPServer(executorService, httpServer, registry, daemon, sampleNameFilter, authenticator);
            } else if (inetSocketAddress != null) {
                assertZero(port, "cannot configure 'inetSocketAddress' and 'port' at the same time");
                assertNull(hostname, "cannot configure 'inetSocketAddress' and 'hostname' at the same time");
                assertNull(inetAddress, "cannot configure 'inetSocketAddress' and 'inetAddress' at the same time");
            } else if (inetAddress != null) {
                assertNull(hostname, "cannot configure 'inetAddress' and 'hostname' at the same time");
                inetSocketAddress = new InetSocketAddress(inetAddress, findPort());
            } else if (hostname != null) {
                inetSocketAddress = new InetSocketAddress(hostname, findPort());
            } else {
                inetSocketAddress = new InetSocketAddress(findPort());
            }

            HttpServer httpServer = null;
            if (httpsConfigurator != null) {
                httpServer = HttpsServer.create(inetSocketAddress, 3);
                ((HttpsServer)httpServer).setHttpsConfigurator(httpsConfigurator);
            } else {
                httpServer = HttpServer.create(inetSocketAddress, 3);
            }

            return new HTTPServer(executorService, httpServer, registry, daemon, sampleNameFilter, authenticator);
        }

        private void assertNull(Object o, String msg) {
            if (o != null) {
                throw new IllegalStateException(msg);
            }
        }

        private void assertZero(int i, String msg) {
            if (i != 0) {
                throw new IllegalStateException(msg);
            }
        }
    }



    private HTTPServer(ExecutorService executorService, HttpServer httpServer, PrometheusRegistry registry, boolean daemon, Predicate<String> sampleNameFilter, Authenticator authenticator) {
        if (httpServer.getAddress() == null)
            throw new IllegalArgumentException("HttpServer hasn't been bound to an address");

        server = httpServer;
        List<ExpositionFormatWriter> expositionFormatWriters = Arrays.asList(
                new PrometheusTextFormatWriter(true),
                new OpenMetricsTextFormatWriter(true),
                new PrometheusProtobufWriter());
        HttpHandler mHandler = new MetricsHandler(registry, expositionFormatWriters, sampleNameFilter);
        HttpHandler hHandler = new HealthyHandler();
        HttpContext mContext = server.createContext("/", mHandler);
        if (authenticator != null) {
            mContext.setAuthenticator(authenticator);
        }
        mContext = server.createContext("/metrics", mHandler);
        if (authenticator != null) {
            mContext.setAuthenticator(authenticator);
        }
        mContext = server.createContext("/-/healthy", hHandler);
        if (authenticator != null) {
            mContext.setAuthenticator(authenticator);
        }
        if (executorService != null) {
            this.executorService = executorService;
        } else {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool(NamedDaemonThreadFactory.defaultThreadFactory(daemon));
            executor.setKeepAliveTime(2, TimeUnit.MINUTES);
            this.executorService = executor;
        }
        server.setExecutor(this.executorService);
        start(daemon);
    }

    /**
     * Start an HTTP server by making sure that its background thread inherit proper daemon flag.
     */
    private void start(boolean daemon) {
        if (daemon == Thread.currentThread().isDaemon()) {
            server.start();
        } else {
            FutureTask<Void> startTask = new FutureTask<Void>(new Runnable() {
                @Override
                public void run() {
                    server.start();
                }
            }, null);
            NamedDaemonThreadFactory.defaultThreadFactory(daemon).newThread(startTask).start();
            try {
                startTask.get();
            } catch (ExecutionException e) {
                throw new RuntimeException("Unexpected exception on starting HTTPSever", e);
            } catch (InterruptedException e) {
                // This is possible only if the current tread has been interrupted,
                // but in real use cases this should not happen.
                // In any case, there is nothing to do, except to propagate interrupted flag.
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Stop the HTTP server.
     * @deprecated renamed to close(), so that the HTTPServer can be used in try-with-resources.
     */
    public void stop() {
        close();
    }

    /**
     * Stop the HTTPServer.
     */
    @Override
    public void close() {
        server.stop(0);
        executorService.shutdown(); // Free any (parked/idle) threads in pool
    }

    /**
     * Gets the port number.
     */
    public int getPort() {
        return server.getAddress().getPort();
    }
}
