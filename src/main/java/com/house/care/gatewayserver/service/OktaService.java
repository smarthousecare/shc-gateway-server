package com.house.care.gatewayserver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.house.care.gatewayserver.service.dto.AuthenticationTokenDTO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OktaService {

    public static final String CLIENT_CREDENTIALS = "client_credentials";

    private static final String SCOPE = "access";

    @Autowired
    ClientCredentialsResourceDetails clientCredentialsResourceDetails;

    public AuthenticationTokenDTO authenticateOkta() {

        RestTemplate restTemplate = ServiceUtils.getLoggingBasicAuthenticationRestTemplate();

        // create form parameters as a MultiValueMap
        MultiValueMap<String, String> formVars = new LinkedMultiValueMap<>();
        formVars.add(OAuth2ParameterNames.GRANT_TYPE, CLIENT_CREDENTIALS);
        formVars.add(OAuth2ParameterNames.CLIENT_ID, clientCredentialsResourceDetails.getClientId());
        formVars.add(OAuth2ParameterNames.CLIENT_SECRET, clientCredentialsResourceDetails.getClientSecret());
        formVars.add(OAuth2ParameterNames.SCOPE, SCOPE);

        AuthenticationTokenDTO authenticationToken = restTemplate.postForObject(clientCredentialsResourceDetails.getAccessTokenUri(), formVars,
                AuthenticationTokenDTO.class);
        log.trace("AUTHENTICATION TOKEN = {}", authenticationToken);
        return authenticationToken;
    }
}
