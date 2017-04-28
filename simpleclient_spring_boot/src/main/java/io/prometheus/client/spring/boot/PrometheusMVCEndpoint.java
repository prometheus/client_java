package io.prometheus.client.spring.boot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.mvc.EndpointMvcAdapter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class PrometheusEndpointProducer extends EndpointMvcAdapter {

	private final PrometheusEndpoint delegate;

	@Autowired
	public PrometheusEndpointProducer(PrometheusEndpoint delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	@RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> invoke() {
		if (delegate.isEnabled()) {
			return delegate.invoke();
		} else {
			return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
		}
	}


}
