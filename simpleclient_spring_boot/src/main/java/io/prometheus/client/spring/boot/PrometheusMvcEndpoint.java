/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.prometheus.client.spring.boot;

import org.springframework.boot.actuate.endpoint.mvc.AbstractEndpointMvcAdapter;
import org.springframework.boot.actuate.endpoint.mvc.HypermediaDisabled;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;

import io.prometheus.client.exporter.common.TextFormat;

/**
 * @author Stuart Williams (pidster)
 */
public class PrometheusMvcEndpoint extends AbstractEndpointMvcAdapter<PrometheusEndpoint> {

    public PrometheusMvcEndpoint(PrometheusEndpoint delegate) {
        super(delegate);
    }

    @Override
    @GetMapping(path = "", produces = TextFormat.CONTENT_TYPE_004)
    @ResponseBody
    @HypermediaDisabled
    public Object invoke() {

        if (!getDelegate().isEnabled()) {
            return new ResponseEntity<Object>(
                    Collections.singletonMap("message", "This endpoint is disabled"),
                    HttpStatus.NOT_FOUND
            );
        }

        return super.invoke();
    }

}
