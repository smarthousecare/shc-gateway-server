package com.house.care.gatewayserver.config;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.client.loadbalancer.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import feign.RequestInterceptor;

@EnableOAuth2Sso
@Configuration
public class OAuth2SsoConfiguration extends WebSecurityConfigurerAdapter {

    private final RequestMatcher authorizationHeaderRequestMatcher;

    public OAuth2SsoConfiguration(@Qualifier("authorizationHeaderRequestMatcher") RequestMatcher authorizationHeaderRequestMatcher) {

        this.authorizationHeaderRequestMatcher = authorizationHeaderRequestMatcher;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .csrf().disable()
                .requestMatcher(new NegatedRequestMatcher(authorizationHeaderRequestMatcher))
                .httpBasic()
                .realmName("Gateway Server")
                .and()
                .authorizeRequests()
                .antMatchers("/services/**").authenticated()
                .antMatchers("/eureka/**").hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/api/profile-info").permitAll()
                .antMatchers("/api/**").authenticated()
                .antMatchers("/config/**").hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/management/health").permitAll()
                .antMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/registry/**").hasAuthority(AuthoritiesConstants.ADMIN)
                .anyRequest().authenticated()
                .and()
                .oauth2Login()
                .and()
                .oauth2ResourceServer().jwt();
    }

    // @Bean
    // public FilterRegistrationBean<OAuth2ClientContextFilter>
    // oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
    //
    // FilterRegistrationBean<OAuth2ClientContextFilter> registration = new
    // FilterRegistrationBean<>();
    // registration.setFilter(filter);
    // registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
    // return registration;
    // }

    @Bean
    public FilterRegistrationBean<CorsFilter> simpleCorsFilter() {

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Collections.singletonList("*"));
        config.setAllowedMethods(Collections.singletonList("*"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    @Bean
    public AuthorizationHeaderFilter authHeaderFilter(OAuth2AuthorizedClientService clientService) {

        return new AuthorizationHeaderFilter(clientService);
    }

    @Bean("userFeignClientInterceptor")
    public RequestInterceptor getUserFeignClientInterceptor(OAuth2AuthorizedClientService clientService) {

        return new UserFeignClientInterceptor(clientService);
    }

    @Bean
    @Qualifier("loadBalancedRestTemplate")
    public RestTemplate loadBalancedRestTemplate(RestTemplateCustomizer customizer) {

        RestTemplate restTemplate = new RestTemplate();
        customizer.customize(restTemplate);
        return restTemplate;
    }

    @Bean
    @Qualifier("vanillaRestTemplate")
    public RestTemplate vanillaRestTemplate() {

        return new RestTemplate();
    }
}
