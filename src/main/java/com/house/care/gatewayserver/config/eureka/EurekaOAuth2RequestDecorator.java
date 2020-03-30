/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.house.care.gatewayserver.config.eureka;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import com.house.care.gatewayserver.service.OktaService;
import com.house.care.gatewayserver.service.dto.AuthenticationTokenDTO;

/**
 * 
 * @author Will Tran
 *
 */
public class EurekaOAuth2RequestDecorator implements DiscoveryRequestDecorator {

    @Autowired
    private OktaService oktaService;

    @Override
    public HttpHeaders getHeaders() {

        // do what
        // org.springframework.security.oauth2.client.OAuth2RestTemplate.createRequest(URI,
        // HttpMethod) does
        // to generate the header
        AuthenticationTokenDTO authenticationTokenDTO = oktaService.authenticateOkta();
        ClientHttpRequest requestHeaderExtrator = new AbstractClientHttpRequest() {

            @Override
            public URI getURI() {

                return null;
            }

            @Override
            public HttpMethod getMethod() {

                return null;
            }

            @Override
            public String getMethodValue() {

                return null;
            }

            @Override
            protected OutputStream getBodyInternal(HttpHeaders headers) throws IOException {

                return null;
            }

            @Override
            protected ClientHttpResponse executeInternal(HttpHeaders headers) throws IOException {

                return null;
            }
        };
        requestHeaderExtrator.getHeaders().set("Authorization", String.format("%s %s", OAuth2AccessToken.BEARER_TYPE, authenticationTokenDTO.getAccessToken()));
        return requestHeaderExtrator.getHeaders();
    }

}
