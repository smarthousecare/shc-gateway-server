package com.house.care.gatewayserver.config.oauth2;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.stereotype.Component;

@Component
public class OAuth2ClientCredentials {

    @Bean
    @ConfigurationProperties("security.oauth2.client")
    protected ClientCredentialsResourceDetails oAuthDetails() {

        return new ClientCredentialsResourceDetails();
    }

    @Bean
    protected OAuth2RestTemplate restTemplate() {

        return new OAuth2RestTemplate(oAuthDetails());
    }

}
