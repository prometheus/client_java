package io.prometheus.metrics.exporter.httpserver;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Expose Prometheus metrics using a plain Java HttpServer.
 * <p>
 * Example Usage:
 * <pre>
 * {@code
 * HTTPServer server = HTTPServer.builder()
 *     .port(9090)
 *     .buildAndStart();
 * }</pre>
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

    protected final HttpServer server;
    protected final ExecutorService executorService;

    private HTTPServer(PrometheusProperties config, ExecutorService executorService, HttpServer httpServer, PrometheusRegistry registry, Authenticator authenticator, HttpHandler defaultHandler) {
        if (httpServer.getAddress() == null) {
            throw new IllegalArgumentException("HttpServer hasn't been bound to an address");
        }
        this.server = httpServer;
        this.executorService = executorService;
        registerHandler("/", defaultHandler == null ? new DefaultHandler() : defaultHandler, authenticator);
        registerHandler("/metrics", new MetricsHandler(config, registry), authenticator);
        registerHandler("/-/healthy", new HealthyHandler(), authenticator);
        try {
            // HttpServer.start() starts the HttpServer in a new background thread.
            // If we call HttpServer.start() from a thread of the executorService,
            // the background thread will inherit the "daemon" property,
            // i.e. the server will run as a Daemon thread.
            // See https://github.com/prometheus/client_java/pull/955
            this.executorService.submit(this.server::start).get();
            // calling .get() on the Future here to avoid silently discarding errors
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerHandler(String path, HttpHandler handler, Authenticator authenticator) {
        HttpContext context = server.createContext(path, handler);
        if (authenticator != null) {
            context.setAuthenticator(authenticator);
        }
    }

    /**
     * Stop the HTTP server. Same as {@link #close()}.
     */
    public void stop() {
        close();
    }

    /**
     * Stop the HTTPServer. Same as {@link #stop()}.
     */
    @Override
    public void close() {
        server.stop(0);
        executorService.shutdown(); // Free any (parked/idle) threads in pool
    }

    /**
     * Gets the port number.
     * This is useful if you did not specify a port and the server picked a free port automatically.
     */
    public int getPort() {
        return server.getAddress().getPort();
    }

    public static Builder builder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder builder(PrometheusProperties config) {
        return new Builder(config);
    }

    public static class Builder {

        private final PrometheusProperties config;
        private Integer port = null;
        private String hostname = null;
        private InetAddress inetAddress = null;
        private ExecutorService executorService = null;
        private PrometheusRegistry registry = null;
        private Authenticator authenticator = null;
        private HttpsConfigurator httpsConfigurator = null;
        private HttpHandler defaultHandler = null;

        private Builder(PrometheusProperties config) {
            this.config = config;
        }

        /**
         * Port to bind to. Default is 0, indicating that a random port will be selected.
         * You can learn the randomly selected port by calling {@link HTTPServer#getPort()}.
         */
        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Use this hostname to resolve the IP address to bind to.
         * Must not be called together with {@link #inetAddress(InetAddress)}.
         * Default is empty, indicating that the HTTPServer binds to the wildcard address.
         */
        public Builder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        /**
         * Bind to this IP address.
         * Must not be called together with {@link #hostname(String)}.
         * Default is empty, indicating that the HTTPServer binds to the wildcard address.
         */
        public Builder inetAddress(InetAddress address) {
            this.inetAddress = address;
            return this;
        }

        /**
         * Optional: ExecutorService used by the {@code httpServer}.
         */
        public Builder executorService(ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

        /**
         * Optional: Default is {@link PrometheusRegistry#defaultRegistry}.
         */
        public Builder registry(PrometheusRegistry registry) {
            this.registry = registry;
            return this;
        }

        /**
         * Optional: {@link Authenticator} for authentication.
         */
        public Builder authenticator(Authenticator authenticator) {
            this.authenticator = authenticator;
            return this;
        }

        /**
         * Optional: {@link HttpsConfigurator} for TLS/SSL
         */
        public Builder httpsConfigurator(HttpsConfigurator configurator) {
            this.httpsConfigurator = configurator;
            return this;
        }

        /**
         * Optional: Override default handler, i.e. the handler that will be registered for the / endpoint.
         */
        public Builder defaultHandler(HttpHandler defaultHandler) {
            this.defaultHandler = defaultHandler;
            return this;
        }

        /**
         * Build and start the HTTPServer.
         */
        public HTTPServer buildAndStart() throws IOException {
            if (registry == null) {
                registry = PrometheusRegistry.defaultRegistry;
            }
            HttpServer httpServer;
            if (httpsConfigurator != null) {
                httpServer = HttpsServer.create(makeInetSocketAddress(), 3);
                ((HttpsServer)httpServer).setHttpsConfigurator(httpsConfigurator);
            } else {
                httpServer = HttpServer.create(makeInetSocketAddress(), 3);
            }
            ExecutorService executorService = makeExecutorService();
            httpServer.setExecutor(executorService);
            return new HTTPServer(config, executorService, httpServer, registry, authenticator, defaultHandler);
        }

        private InetSocketAddress makeInetSocketAddress() {
            if (inetAddress != null) {
                assertNull(hostname, "cannot configure 'inetAddress' and 'hostname' at the same time");
                return new InetSocketAddress(inetAddress, findPort());
            } else if (hostname != null) {
                return new InetSocketAddress(hostname, findPort());
            } else {
                return new InetSocketAddress(findPort());
            }
        }

        private ExecutorService makeExecutorService() {
            if (executorService != null) {
                return executorService;
            } else {
                return new ThreadPoolExecutor(
                                1,
                                10,
                                120,
                                TimeUnit.SECONDS,
                                new SynchronousQueue<>(true),
                                NamedDaemonThreadFactory.defaultThreadFactory(true),
                                new BlockingRejectedExecutionHandler());
            }
        }

        private int findPort() {
            if (config != null && config.getExporterHttpServerProperties() != null) {
                Integer port = config.getExporterHttpServerProperties().getPort();
                if (port != null) {
                    return port;
                }
            }
            if (port != null) {
                return port;
            }
            return 0; // random port will be selected
        }

        private void assertNull(Object o, String msg) {
            if (o != null) {
                throw new IllegalStateException(msg);
            }
        }
    }

    private static class BlockingRejectedExecutionHandler implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
            if (!threadPoolExecutor.isShutdown()) {
                try {
                    threadPoolExecutor.getQueue().put(runnable);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
