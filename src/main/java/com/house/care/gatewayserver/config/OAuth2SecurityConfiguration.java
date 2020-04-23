package com.house.care.gatewayserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.house.care.gatewayserver.config.oauth2.CookieTokenExtractor;
import com.house.care.gatewayserver.config.oauth2.SimpleAuthoritiesExtractor;
import com.house.care.gatewayserver.config.oauth2.SimplePrincipalExtractor;
import com.house.care.gatewayserver.service.OktaService;
import com.house.care.gatewayserver.web.rest.filter.RefreshTokenFilterConfigurer;

@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class OAuth2SecurityConfiguration extends ResourceServerConfigurerAdapter {

    private static final String OAUTH2_PRINCIPAL_ATTRIBUTE = "preferred_username";

    private static final String OAUTH2_AUTHORITIES_ATTRIBUTE = "groups";

    @Autowired
    private OktaService oktaService;

    @Value("${security.oauth2.client.clientId}")
    private String clientIdentifier;

    @Bean
    public PrincipalExtractor principalExtractor() {

        return new SimplePrincipalExtractor(OAUTH2_PRINCIPAL_ATTRIBUTE);
    }

    @Bean
    public AuthoritiesExtractor authoritiesExtractor() {

        return new SimpleAuthoritiesExtractor(OAUTH2_AUTHORITIES_ATTRIBUTE);
    }

    @Bean
    @Primary
    public UserInfoTokenServices userInfoTokenServices(PrincipalExtractor principalExtractor, AuthoritiesExtractor authoritiesExtractor,
            ResourceServerProperties resourceServerProperties) {

        UserInfoTokenServices userInfoTokenServices = new UserInfoTokenServices(resourceServerProperties.getUserInfoUri(), resourceServerProperties.getClientId());
        userInfoTokenServices.setPrincipalExtractor(principalExtractor);
        userInfoTokenServices.setAuthoritiesExtractor(authoritiesExtractor);
        return userInfoTokenServices;
    }

    @Bean
    @Qualifier("authorizationHeaderRequestMatcher")
    public RequestMatcher authorizationHeaderRequestMatcher() {

        return new RequestHeaderRequestMatcher("Authorization");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {

        http
                .csrf()
                .disable()
                .headers()
                .frameOptions()
                .disable()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .requestMatcher(authorizationHeaderRequestMatcher())
                .authorizeRequests()
                .antMatchers("/services/**").authenticated()
                .antMatchers("/api/profile-info").permitAll()
                .antMatchers("/api/**").authenticated()
                .antMatchers("/management/health").permitAll()
                .antMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/v3/api-docs").permitAll().and().apply(refreshTokenFilterConfigurer());
    }

    private RefreshTokenFilterConfigurer refreshTokenFilterConfigurer() {

        return new RefreshTokenFilterConfigurer(oktaService);
    }

    /**
     * Configure the ResourceServer security by installing a new TokenExtractor.
     */
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {

        resources.tokenExtractor(tokenExtractor());
        resources.resourceId(clientIdentifier);
    }

    /**
     * The new TokenExtractor can extract tokens from Cookies and Authorization
     * headers.
     *
     * @return the CookieTokenExtractor bean.
     */
    @Bean
    public TokenExtractor tokenExtractor() {

        return new CookieTokenExtractor();
    }
}
