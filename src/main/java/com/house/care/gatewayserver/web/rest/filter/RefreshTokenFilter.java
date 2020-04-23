package com.house.care.gatewayserver.web.rest.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.common.exceptions.ClientAuthenticationException;
import org.springframework.web.filter.GenericFilterBean;

import com.house.care.gatewayserver.config.oauth2.CookiesHttpServletRequestWrapper;
import com.house.care.gatewayserver.config.oauth2.OAuth2CookieHelper;
import com.house.care.gatewayserver.config.oauth2.OAuth2Cookies;
import com.house.care.gatewayserver.service.OktaService;

public class RefreshTokenFilter extends GenericFilterBean {

    /**
     * Number of seconds before expiry to start refreshing access tokens so we
     * don't run into race conditions when forwarding requests downstream.
     * Otherwise, access tokens may still be valid when we check here but may
     * then be expired when relayed to another microservice a wee bit later.
     */
    private static final int REFRESH_WINDOW_SECS = 30;

    private final OktaService oktaService;

    private final Logger log = LoggerFactory.getLogger(RefreshTokenFilter.class);

    public RefreshTokenFilter(OktaService oktaService) {

        this.oktaService = oktaService;
    }

    /**
     * Check access token cookie and refresh it, if it is either not present,
     * expired or about to expire.
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        try {

            OAuth2CookieHelper oAuth2CookieHelper = new OAuth2CookieHelper();

            Cookie authTokenCookie = OAuth2CookieHelper.getAccessTokenCookie(httpServletRequest);
            String token = authTokenCookie != null ? authTokenCookie.getValue() : oktaService.getCurrentAuthenticationToken();
            Cookie refreshTokenCookie = OAuth2CookieHelper.getRefreshTokenCookie(httpServletRequest);
            String refreshToken = refreshTokenCookie != null ? refreshTokenCookie.getValue() : null;

            if (token != null) {
                OAuth2Cookies oAuth2Cookies = new OAuth2Cookies();
                oAuth2CookieHelper.createCookies(httpServletRequest, token, refreshToken, oAuth2Cookies);
                Cookie[] cookies = oAuth2CookieHelper.stripCookies(httpServletRequest.getCookies(), oAuth2Cookies);
                httpServletRequest = new CookiesHttpServletRequestWrapper(httpServletRequest, cookies);
            }
        }
        catch (ClientAuthenticationException ex) {
            log.warn("Security exception: could not refresh tokens", ex);
        }
        filterChain.doFilter(httpServletRequest, servletResponse);
    }

}
