package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.annotations.StableApi;
import javax.annotation.Nullable;

/** Infos extracted from the request received by the endpoint */
@StableApi
public interface PrometheusScrapeRequest {

  /** Absolute path of the HTTP request. */
  String getRequestPath();

  /** See {@code jakarta.servlet.ServletRequest.getParameterValues(String name)} */
  @Nullable
  String[] getParameterValues(String name);
}
