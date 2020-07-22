package com.house.care.gatewayserver.config;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
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

        String tokenType = null;
        String tokenValue = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

            OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName());

            OAuth2AccessToken accessToken = client.getAccessToken();
            tokenType = accessToken.getTokenType().getValue();
            tokenValue = accessToken.getTokenValue();
        }
        else if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
            tokenValue = jwtAuthenticationToken.getToken().getTokenValue();
            tokenType = OAuth2AccessToken.TokenType.BEARER.getValue();
        }
        else {
            return Optional.empty();
        }

        String authorizationHeaderValue = String.format("%s %s", tokenType, tokenValue);
        return Optional.of(authorizationHeaderValue);
    }
}