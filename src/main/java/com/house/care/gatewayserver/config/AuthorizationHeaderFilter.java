package com.house.care.gatewayserver.config;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

import java.util.Optional;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

public class AuthorizationHeaderFilter extends ZuulFilter {

    private final OAuth2AuthorizedClientService clientService;

    public AuthorizationHeaderFilter(OAuth2AuthorizedClientService clientService) {

        this.clientService = clientService;
    }

    @Override
    public String filterType() {

        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {

        return PRE_DECORATION_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {

        return true;
    }

    @Override
    public Object run() {

        RequestContext ctx = RequestContext.getCurrentContext();
        Optional<String> authorizationHeader = getAuthorizationHeader();
        authorizationHeader.ifPresent(s -> ctx.addZuulRequestHeader("Authorization", s));
        return null;
    }

    private Optional<String> getAuthorizationHeader() {

        String token = null;
        String tokenType = null;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AbstractAuthenticationToken oauthToken = null;
        if (authentication instanceof OAuth2AuthenticationToken) {
            oauthToken = (OAuth2AuthenticationToken) authentication;

            OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                    ((OAuth2AuthenticationToken) oauthToken).getAuthorizedClientRegistrationId(),
                    oauthToken.getName());

            OAuth2AccessToken accessToken = client.getAccessToken();
            if (accessToken == null) {
                return Optional.empty();
            }
            else {
                token = accessToken.getTokenValue();
                tokenType = accessToken.getTokenType().getValue();
            }

        }
        else if (authentication instanceof OAuth2Authentication) {
            oauthToken = (OAuth2Authentication) authentication;
            OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) oauthToken.getDetails();
            if (details == null) {
                return Optional.empty();
            }
            else {
                token = details.getTokenValue();
                tokenType = details.getTokenType();
            }
        }
        else if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtOAuthToken = (JwtAuthenticationToken) authentication;
            Jwt credentials = jwtOAuthToken.getToken();
            if (credentials == null) {
                return Optional.empty();
            }
            else {
                token = credentials.getTokenValue();
                tokenType = "Bearer";
            }
        }

        if (token == null) {
            return Optional.empty();
        }
        else {
            String authorizationHeaderValue = String.format("%s %s", tokenType, token);
            return Optional.of(authorizationHeaderValue);
        }
    }
}