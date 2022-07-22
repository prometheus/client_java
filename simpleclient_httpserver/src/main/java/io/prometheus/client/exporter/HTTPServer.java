package io.prometheus.client.exporter;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Predicate;
import io.prometheus.client.SampleNameFilter;
import io.prometheus.client.Supplier;
import io.prometheus.client.exporter.common.TextFormat;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;

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

    private static class LocalByteArray extends ThreadLocal<ByteArrayOutputStream> {
        @Override
        protected ByteArrayOutputStream initialValue()
        {
            return new ByteArrayOutputStream(1 << 20);
        }
    }

    /**
     * Handles Metrics collections from the given registry.
     */
    public static class HTTPMetricHandler implements HttpHandler {
        private final CollectorRegistry registry;
        private final LocalByteArray response = new LocalByteArray();
        private final Supplier<Predicate<String>> sampleNameFilterSupplier;
        private final static String HEALTHY_RESPONSE = "Exporter is Healthy.";

        public HTTPMetricHandler(CollectorRegistry registry) {
            this(registry, null);
        }

        public HTTPMetricHandler(CollectorRegistry registry, Supplier<Predicate<String>> sampleNameFilterSupplier) {
            this.registry = registry;
            this.sampleNameFilterSupplier = sampleNameFilterSupplier;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getRawQuery();
            String contextPath = t.getHttpContext().getPath();
            ByteArrayOutputStream response = this.response.get();
            response.reset();
            OutputStreamWriter osw = new OutputStreamWriter(response, Charset.forName("UTF-8"));
            if ("/-/healthy".equals(contextPath)) {
                osw.write(HEALTHY_RESPONSE);
            } else {
                String contentType = TextFormat.chooseContentType(t.getRequestHeaders().getFirst("Accept"));
                t.getResponseHeaders().set("Content-Type", contentType);
                Predicate<String> filter = sampleNameFilterSupplier == null ? null : sampleNameFilterSupplier.get();
                filter = SampleNameFilter.restrictToNamesEqualTo(filter, parseQuery(query));
                if (filter == null) {
                    TextFormat.writeFormat(contentType, osw, registry.metricFamilySamples());
                } else {
                    TextFormat.writeFormat(contentType, osw, registry.filteredMetricFamilySamples(filter));
                }
            }

            osw.close();

            if (shouldUseCompression(t)) {
                t.getResponseHeaders().set("Content-Encoding", "gzip");
                t.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                final GZIPOutputStream os = new GZIPOutputStream(t.getResponseBody());
                try {
                    response.writeTo(os);
                } finally {
                    os.close();
                }
            } else {
                long contentLength = response.size();
                if (contentLength > 0) {
                    t.getResponseHeaders().set("Content-Length", String.valueOf(contentLength));
                }
                if (t.getRequestMethod().equals("HEAD")) {
                    contentLength = -1;
                }
                t.sendResponseHeaders(HttpURLConnection.HTTP_OK, contentLength);
                response.writeTo(t.getResponseBody());
            }
            t.close();
        }
    }

    protected static boolean shouldUseCompression(HttpExchange exchange) {
        List<String> encodingHeaders = exchange.getRequestHeaders().get("Accept-Encoding");
        if (encodingHeaders == null) return false;

        for (String encodingHeader : encodingHeaders) {
            String[] encodings = encodingHeader.split(",");
            for (String encoding : encodings) {
                if (encoding.trim().equalsIgnoreCase("gzip")) {
                    return true;
                }
            }
        }
        return false;
    }

    protected static Set<String> parseQuery(String query) throws IOException {
        Set<String> names = new HashSet<String>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx != -1 && URLDecoder.decode(pair.substring(0, idx), "UTF-8").equals("name[]")) {
                    names.add(URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                }
            }
        }
        return names;
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

    /**
     * We keep the original constructors of {@link HTTPServer} for compatibility, but new configuration
     * parameters like {@code sampleNameFilter} must be configured using the Builder.
     */
    public static class Builder {

        private int port = 0;
        private String hostname = null;
        private InetAddress inetAddress = null;
        private InetSocketAddress inetSocketAddress = null;
        private HttpServer httpServer = null;
        private ExecutorService executorService = null;
        private CollectorRegistry registry = CollectorRegistry.defaultRegistry;
        private boolean daemon = false;
        private Predicate<String> sampleNameFilter;
        private Supplier<Predicate<String>> sampleNameFilterSupplier;
        private Authenticator authenticator;
        private HttpsConfigurator httpsConfigurator;

        /**
         * Port to bind to. Must not be called together with {@link #withInetSocketAddress(InetSocketAddress)}
         * or {@link #withHttpServer(HttpServer)}. Default is 0, indicating that a random port will be selected.
         */
        public Builder withPort(int port) {
            this.port = port;
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
         * <p>
         * Use this if the sampleNameFilter remains the same throughout the lifetime of the HTTPServer.
         * If the sampleNameFilter changes during runtime, use {@link #withSampleNameFilterSupplier(Supplier)}.
         */
        public Builder withSampleNameFilter(Predicate<String> sampleNameFilter) {
            this.sampleNameFilter = sampleNameFilter;
            return this;
        }

        /**
         * Optional: Only export time series where {@code sampleNameFilter.test(name)} returns true.
         * <p>
         * Use this if the sampleNameFilter may change during runtime, like for example if you have a
         * hot reload mechanism for your filter config.
         * If the sampleNameFilter remains the same throughout the lifetime of the HTTPServer,
         * use {@link #withSampleNameFilter(Predicate)} instead.
         */
        public Builder withSampleNameFilterSupplier(Supplier<Predicate<String>> sampleNameFilterSupplier) {
            this.sampleNameFilterSupplier = sampleNameFilterSupplier;
            return this;
        }

        /**
         * Optional: Default is {@link CollectorRegistry#defaultRegistry}.
         */
        public Builder withRegistry(CollectorRegistry registry) {
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

        /**
         * Build the HTTPServer
         * @throws IOException
         */
        public HTTPServer build() throws IOException {
            if (sampleNameFilter != null) {
                assertNull(sampleNameFilterSupplier, "cannot configure 'sampleNameFilter' and 'sampleNameFilterSupplier' at the same time");
                sampleNameFilterSupplier = SampleNameFilterSupplier.of(sampleNameFilter);
            }

            if (httpServer != null) {
                assertNull(executorService, "cannot configure 'httpServer' and `executorService'");
                assertZero(port, "cannot configure 'httpServer' and 'port' at the same time");
                assertNull(hostname, "cannot configure 'httpServer' and 'hostname' at the same time");
                assertNull(inetAddress, "cannot configure 'httpServer' and 'inetAddress' at the same time");
                assertNull(inetSocketAddress, "cannot configure 'httpServer' and 'inetSocketAddress' at the same time");
                assertNull(httpsConfigurator, "cannot configure 'httpServer' and 'httpsConfigurator' at the same time");
                return new HTTPServer(executorService, httpServer, registry, daemon, sampleNameFilterSupplier, authenticator);
            } else if (inetSocketAddress != null) {
                assertZero(port, "cannot configure 'inetSocketAddress' and 'port' at the same time");
                assertNull(hostname, "cannot configure 'inetSocketAddress' and 'hostname' at the same time");
                assertNull(inetAddress, "cannot configure 'inetSocketAddress' and 'inetAddress' at the same time");
            } else if (inetAddress != null) {
                assertNull(hostname, "cannot configure 'inetAddress' and 'hostname' at the same time");
                inetSocketAddress = new InetSocketAddress(inetAddress, port);
            } else if (hostname != null) {
                inetSocketAddress = new InetSocketAddress(hostname, port);
            } else {
                inetSocketAddress = new InetSocketAddress(port);
            }

            HttpServer httpServer = null;
            if (httpsConfigurator != null) {
                httpServer = HttpsServer.create(inetSocketAddress, 3);
                ((HttpsServer)httpServer).setHttpsConfigurator(httpsConfigurator);
            } else {
                httpServer = HttpServer.create(inetSocketAddress, 3);
            }

            return new HTTPServer(executorService, httpServer, registry, daemon, sampleNameFilterSupplier, authenticator);
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

    /**
     * Start an HTTP server serving Prometheus metrics from the given registry using the given {@link HttpServer}.
     * The {@code httpServer} is expected to already be bound to an address
     */
    public HTTPServer(HttpServer httpServer, CollectorRegistry registry, boolean daemon) throws IOException {
        this(null, httpServer, registry, daemon, null, null);
    }

    /**
     * Start an HTTP server serving Prometheus metrics from the given registry.
     */
    public HTTPServer(InetSocketAddress addr, CollectorRegistry registry, boolean daemon) throws IOException {
        this(HttpServer.create(addr, 3), registry, daemon);
    }

    /**
     * Start an HTTP server serving Prometheus metrics from the given registry using non-daemon threads.
     */
    public HTTPServer(InetSocketAddress addr, CollectorRegistry registry) throws IOException {
        this(addr, registry, false);
    }

    /**
     * Start an HTTP server serving the default Prometheus registry.
     */
    public HTTPServer(int port, boolean daemon) throws IOException {
        this(new InetSocketAddress(port), CollectorRegistry.defaultRegistry, daemon);
    }

    /**
     * Start an HTTP server serving the default Prometheus registry using non-daemon threads.
     */
    public HTTPServer(int port) throws IOException {
        this(port, false);
    }

    /**
     * Start an HTTP server serving the default Prometheus registry.
     */
    public HTTPServer(String host, int port, boolean daemon) throws IOException {
        this(new InetSocketAddress(host, port), CollectorRegistry.defaultRegistry, daemon);
    }

    /**
     * Start an HTTP server serving the default Prometheus registry using non-daemon threads.
     */
    public HTTPServer(String host, int port) throws IOException {
        this(new InetSocketAddress(host, port), CollectorRegistry.defaultRegistry, false);
    }

    private HTTPServer(ExecutorService executorService, HttpServer httpServer, CollectorRegistry registry, boolean daemon, Supplier<Predicate<String>> sampleNameFilterSupplier, Authenticator authenticator) {
        if (httpServer.getAddress() == null)
            throw new IllegalArgumentException("HttpServer hasn't been bound to an address");

        server = httpServer;
        HttpHandler mHandler = new HTTPMetricHandler(registry, sampleNameFilterSupplier);
        HttpContext mContext = server.createContext("/", mHandler);
        if (authenticator != null) {
            mContext.setAuthenticator(authenticator);
        }
        mContext = server.createContext("/metrics", mHandler);
        if (authenticator != null) {
            mContext.setAuthenticator(authenticator);
        }
        mContext = server.createContext("/-/healthy", mHandler);
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
