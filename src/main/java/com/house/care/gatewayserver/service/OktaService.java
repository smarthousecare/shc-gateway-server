package com.house.care.gatewayserver.service;

import java.time.Instant;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.house.care.gatewayserver.service.dto.AuthenticationTokenDTO;
import com.house.care.gatewayserver.service.dto.SHCClientProperties;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OktaService {

    public static final String CLIENT_CREDENTIALS = "client_credentials";

    private static final String SCOPE = "access";

    @Autowired
    private SHCClientProperties shcClientProperties;

    @Autowired
    private OAuth2AuthorizedClientService clientService;

    public AuthenticationTokenDTO authenticateOkta() {

        RestTemplate restTemplate = ServiceUtils.getLoggingBasicAuthenticationRestTemplate();

        // create form parameters as a MultiValueMap
        MultiValueMap<String, String> formVars = new LinkedMultiValueMap<>();
        formVars.add(OAuth2ParameterNames.GRANT_TYPE, CLIENT_CREDENTIALS);
        formVars.add(OAuth2ParameterNames.CLIENT_ID, shcClientProperties.getClientId());
        formVars.add(OAuth2ParameterNames.CLIENT_SECRET, shcClientProperties.getClientSecret());
        formVars.add(OAuth2ParameterNames.SCOPE, SCOPE);

        AuthenticationTokenDTO authenticationToken = restTemplate.postForObject(shcClientProperties.getAccessTokenUri(), formVars,
                AuthenticationTokenDTO.class);
        log.trace("AUTHENTICATION TOKEN = {}", authenticationToken);
        return authenticationToken;
    }

    public String getCurrentAuthenticationToken() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        if (oauthToken != null) {
            OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName());

            OAuth2AccessToken accessToken = client.getAccessToken();
            if (accessToken == null || Instant.now().isBefore(Objects.requireNonNull(accessToken.getExpiresAt()))) {
                AuthenticationTokenDTO authenticationTokenDTO = authenticateOkta();
                return authenticationTokenDTO.getAccessToken();
            }
            else {
                return accessToken.getTokenValue();
            }
        }
        else {
            return null;
        }
    }
}
