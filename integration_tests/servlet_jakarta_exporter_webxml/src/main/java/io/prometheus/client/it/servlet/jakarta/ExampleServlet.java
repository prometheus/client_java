package io.prometheus.client.it.servlet.jakarta;

import io.prometheus.client.hotspot.DefaultExports;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

public class ExampleServlet extends HttpServlet {

    @Override
    public void init() {
        DefaultExports.initialize();
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        resp.setStatus(200);
        resp.setContentType("text/plain");
        Writer writer = new BufferedWriter(resp.getWriter());
        writer.write("Hello, world!\n");
        writer.close();
    }
}
