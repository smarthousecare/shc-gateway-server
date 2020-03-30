package com.house.care.gatewayserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

@EnableFeignClients
@EnableCircuitBreaker
@EnableDiscoveryClient
@EnableEurekaClient
@EnableZuulProxy
@SpringBootApplication
@EnableOAuth2Sso
@EnableResourceServer
public class GatewayServerApplication {

    public static void main(String[] args) {

        SpringApplication.run(GatewayServerApplication.class, args);
    }

}
