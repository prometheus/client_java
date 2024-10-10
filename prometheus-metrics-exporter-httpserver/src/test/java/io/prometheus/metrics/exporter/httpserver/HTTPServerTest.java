package io.prometheus.metrics.exporter.httpserver;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpPrincipal;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.Principal;
import javax.security.auth.Subject;
import org.junit.jupiter.api.Test;

public class HTTPServerTest {

  @Test
  public void testSubjectDoAs() throws Exception {

    final String user = "joe";
    final Subject subject = new Subject();
    subject.getPrincipals().add(() -> user);

    Authenticator authenticator =
        new Authenticator() {
          @Override
          public Result authenticate(HttpExchange exchange) {
            exchange.setAttribute("aa", subject);
            return new Success(new HttpPrincipal(user, "/"));
          }
        };

    HttpHandler handler =
        exchange -> {
          boolean found = false;
          Subject current = Subject.getSubject(AccessController.getContext());
          for (Principal p : current.getPrincipals()) {
            if (user.equals(p.getName())) {
              found = true;
              break;
            }
          }
          if (!found) {
            throw new IOException("Expected validated user joe!");
          }
          exchange.sendResponseHeaders(204, -1);
        };
    HTTPServer server =
        HTTPServer.builder()
            .port(0)
            .authenticator(authenticator)
            .defaultHandler(handler)
            .authenticatedSubjectAttributeName("aa")
            .buildAndStart();

    Socket socket = new Socket();
    try {
      socket.connect(new InetSocketAddress("localhost", server.getPort()));

      socket.getOutputStream().write("GET / HTTP/1.1 \r\n".getBytes(StandardCharsets.UTF_8));
      socket.getOutputStream().write("HOST: localhost \r\n\r\n".getBytes(StandardCharsets.UTF_8));
      socket.getOutputStream().flush();

      String actualResponse = "";
      byte[] resp = new byte[500];
      int read = socket.getInputStream().read(resp, 0, resp.length);
      if (read > 0) {
        actualResponse = new String(resp, 0, read, StandardCharsets.UTF_8);
      }
      assertThat(actualResponse).contains("204");

    } finally {
      socket.close();
    }
  }
}
