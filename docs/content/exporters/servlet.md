---
title: Servlet
weight: 4
---

The [PrometheusMetricsServlet](/client_java/api/io/prometheus/metrics/exporter/servlet/jakarta/PrometheusMetricsServlet.html) is a [Jakarta Servlet](https://jakarta.ee/specifications/servlet/) for exposing a metric endpoint.

web.xml
-------

The old-school way of configuring a servlet is in a `web.xml` file:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-app_5_0.xsd"
             version="5.0">
    <servlet>
        <servlet-name>prometheus-metrics</servlet-name>
        <servlet-class>io.prometheus.metrics.exporter.servlet.jakarta.PrometheusMetricsServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>prometheus-metrics</servlet-name>
        <url-pattern>/metrics</url-pattern>
    </servlet-mapping>
</web-app>
```

Programmatic
------------

Today, most Servlet applications use an embedded Servlet container and configure Servlets programmatically rather than via `web.xml`.
The API for that depends on the Servlet container.
The [examples](https://github.com/prometheus/client_java/tree/1.0.x/examples) directory has an example of an embedded [Tomcat](https://tomcat.apache.org/) container with the [PrometheusMetricsServlet](/client_java/api/io/prometheus/metrics/exporter/servlet/jakarta/PrometheusMetricsServlet.html) configured.

Spring
------

You can use the [PrometheusMetricsServlet](/client_java/api/io/prometheus/metrics/exporter/servlet/jakarta/PrometheusMetricsServlet.html) in Spring applications. See [our Spring doc]({{< relref "spring.md" >}}).
