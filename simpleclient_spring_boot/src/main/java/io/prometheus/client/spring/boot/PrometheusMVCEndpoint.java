package io.prometheus.client.spring.boot;

import org.springframework.boot.actuate.endpoint.mvc.EndpointMvcAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import io.prometheus.client.exporter.common.TextFormat;

@ConfigurationProperties("endpoints.prometheus")
public class PrometheusMVCEndpoint extends EndpointMvcAdapter {

	private final PrometheusEndpoint delegate;
	
	public PrometheusMVCEndpoint(PrometheusEndpoint delegate) {
		super(delegate);
		this.delegate = delegate;
	}
	
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping( produces={TextFormat.CONTENT_TYPE_004})
	public ResponseEntity<String> metrics() {
		if (!this.delegate.isEnabled()) {
			// Shouldn't happen - MVC endpoint shouldn't be registered when delegate's
			// disabled
			return (ResponseEntity<String>) getDisabledResponse();
		}
		
		ResponseEntity<String> result = this.delegate.invoke();
		
		return result;
	}

}
