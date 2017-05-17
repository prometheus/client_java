package io.prometheus.client.spring.boot;

import io.prometheus.client.exporter.common.TextFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.mvc.AbstractEndpointMvcAdapter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class PrometheusMVCEndpoint extends AbstractEndpointMvcAdapter<PrometheusEndpoint> {

	@Autowired
	public PrometheusMVCEndpoint(PrometheusEndpoint delegate) {
		super(delegate);
	}

	@RequestMapping(value = "", method = RequestMethod.GET, produces = TextFormat.CONTENT_TYPE_004)
	public Object invoke() {
		if (!getDelegate().isEnabled()) {
			// Shouldn't happen because the request mapping should not be registered
			return getDisabledResponse();
		} else {
			return getDelegate().invoke();
		}
	}
}