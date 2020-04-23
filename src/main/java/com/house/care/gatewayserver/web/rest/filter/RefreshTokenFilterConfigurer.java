package com.house.care.gatewayserver.web.rest.filter;

import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;
import org.springframework.security.web.DefaultSecurityFilterChain;

import com.house.care.gatewayserver.service.OktaService;

/**
 * Configures a RefreshTokenFilter to refresh access tokens if they are about to
 * expire.
 *
 * @see RefreshTokenFilter
 */
public class RefreshTokenFilterConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private final OktaService oktaService;

    public RefreshTokenFilterConfigurer(OktaService oktaService) {

        this.oktaService = oktaService;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {

        RefreshTokenFilter customFilter = new RefreshTokenFilter(oktaService);
        http.addFilterBefore(customFilter, OAuth2AuthenticationProcessingFilter.class);
    }
}
