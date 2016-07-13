package io.prometheus.client.vertx;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;

import java.io.IOException;
import java.io.Writer;

/**
 * Metrics Handler for Vert.x Web.
 *
 * This handler will allow the usage of Prometheus Client Java API with
 * Vert.x applications and expose a API compatible handler for the collector.
 *
 * Usage:
 *
 *     router.route("/metrics").handler(new MetricsHandler());
 */
public class MetricsHandler implements Handler<RoutingContext> {

  /**
   * Wrap a Vert.x Buffer as a Writer so it can be used with
   * TextFormat writer
   */
  private static class BufferWriter extends Writer {

    private final Buffer buffer = Buffer.buffer();

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
      buffer.appendString(new String(cbuf, off, len));
    }

    @Override
    public void flush() throws IOException {
      // NO-OP
    }

    @Override
    public void close() throws IOException {
      // NO-OP
    }

    Buffer getBuffer() {
      return buffer;
    }
  }

  private CollectorRegistry registry;

  /**
   * Construct a MetricsHandler for the default registry.
   */
  public MetricsHandler() {
    this(CollectorRegistry.defaultRegistry);
  }

  /**
   * Construct a MetricsHandler for the given registry.
   */
  public MetricsHandler(CollectorRegistry registry) {
    this.registry = registry;
  }

  @Override
  public void handle(RoutingContext ctx) {
    try {
      final BufferWriter writer = new BufferWriter();
      TextFormat.write004(writer, registry.metricFamilySamples());
      ctx.response()
              .setStatusCode(200)
              .putHeader("Content-Type", TextFormat.CONTENT_TYPE_004)
              .end(writer.getBuffer());
    } catch (IOException e) {
      ctx.fail(e);
    }
  }
}
