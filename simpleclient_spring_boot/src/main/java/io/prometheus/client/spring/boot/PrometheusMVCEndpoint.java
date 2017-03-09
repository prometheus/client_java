package io.prometheus.client.spring.boot;

import org.springframework.boot.actuate.endpoint.mvc.EndpointMvcAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@ConfigurationProperties("endpoints.prometheus")
public class PrometheusMVCEndpoint extends EndpointMvcAdapter {

	private final PrometheusEndpoint delegate;
	
	public PrometheusMVCEndpoint(PrometheusEndpoint delegate) {
		super(delegate);
		this.delegate = delegate;
	}
	
	@ResponseBody
	@RequestMapping(value="/metrics", produces={"text/plain;version=0.0.4"})
	public String metrics() {
		if (!this.delegate.isEnabled()) {
			// Shouldn't happen - MVC endpoint shouldn't be registered when delegate's
			// disabled
			return getDisabledResponse().toString();
		}
		
		ResponseEntity<String> result = this.delegate.invoke();
		
		return result.getBody();
	}

}
