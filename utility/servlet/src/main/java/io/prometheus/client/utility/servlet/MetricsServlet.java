/*
 * Copyright 2013 Prometheus Team Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.prometheus.client.utility.servlet;

import com.matttproud.accepts.Accept;
import com.matttproud.accepts.Parser;
import io.prometheus.client.Prometheus;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
public class MetricsServlet extends HttpServlet {
  private void dumpJson(final HttpServletResponse resp) throws IOException {
    resp.setContentType("application/json; schema=\"prometheus/telemetry\"; version=0.0.2");
    Prometheus.defaultDumpJson(resp.getWriter());
  }

  private void dumpProto(final HttpServletResponse resp) throws IOException {
    resp.setContentType("application/vnd.google.protobuf; proto=\"io.prometheus.client.MetricFamily\"; encoding=\"delimited\"");
    final BufferedOutputStream buf = new BufferedOutputStream(resp.getOutputStream());
    Prometheus.defaultDumpProto(buf);
    buf.close();
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setStatus(HttpServletResponse.SC_OK);
    boolean dispensed = false;
    try {
      for (final Accept spec : Parser.parse(req)) {
        if ("application".equals(spec.getType()) && "vnd.google.protobuf".equals(spec.getSubtype())) {
          final Map<String, String> params = spec.getParams();
          if ("io.prometheus.client.MetricFamily".equals(params.get("proto"))
              && "delimited".equals(params.get("encoding"))) {
            dumpProto(resp);
            dispensed = true;
            break;
          }
        }
      }
    } catch (final IllegalArgumentException e) {
    } finally {
      if (!dispensed) {
        dumpJson(resp);
      }
    }
  }
}
