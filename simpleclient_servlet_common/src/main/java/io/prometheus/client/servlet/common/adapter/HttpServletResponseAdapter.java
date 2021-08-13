package io.prometheus.client.servlet.common.adapter;

import java.io.IOException;
import java.io.PrintWriter;

public interface HttpServletResponseAdapter {
    int getStatus();
    void setStatus(int httpStatusCode);
    void setContentType(String contentType);
    PrintWriter getWriter() throws IOException;
}
