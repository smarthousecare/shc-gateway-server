package com.house.care.gatewayserver.service.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "shc.oauth2.client.registration")
public class SHCClientProperties {
    private String registrationId;
    private String accessTokenUri;
    private String userAuthorizationUri;
    private String clientId;
    private String clientSecret;
    private String scope;
    private String userInfoUri;
}
