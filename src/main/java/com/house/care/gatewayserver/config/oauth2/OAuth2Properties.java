package com.house.care.gatewayserver.config.oauth2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OAuth2 properties define properties for OAuth2-based microservices.
 */
@Component
@ConfigurationProperties(prefix = "oauth2", ignoreUnknownFields = false)
public class OAuth2Properties {

    private WebClientConfiguration webClientConfiguration = new WebClientConfiguration();

    public WebClientConfiguration getWebClientConfiguration() {

        return webClientConfiguration;
    }

    public static class WebClientConfiguration {

        @Value("${oauth2.web-client-configuration.client-id}")
        private String clientId;

        @Value("${oauth2.web-client-configuration.secret}")
        private String secret;

        private int sessionTimeoutInSeconds = 1800;

        private String cookieDomain;

        public String getClientId() {

            return clientId;
        }

        public void setClientId(String clientId) {

            this.clientId = clientId;
        }

        public String getSecret() {

            return secret;
        }

        public void setSecret(String secret) {

            this.secret = secret;
        }

        public int getSessionTimeoutInSeconds() {

            return sessionTimeoutInSeconds;
        }

        public void setSessionTimeoutInSeconds(int sessionTimeoutInSeconds) {

            this.sessionTimeoutInSeconds = sessionTimeoutInSeconds;
        }

        public String getCookieDomain() {

            return cookieDomain;
        }

        public void setCookieDomain(String cookieDomain) {

            this.cookieDomain = cookieDomain;
        }
    }
}
