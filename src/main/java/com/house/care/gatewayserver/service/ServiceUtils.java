package com.house.care.gatewayserver.service;

import java.beans.FeatureDescriptor;
import java.util.Arrays;
import java.util.stream.Stream;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.house.care.gatewayserver.config.RequestLoggingInterceptor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ServiceUtils {

    private ServiceUtils() {

    }

    public static String[] getNullPropertyNames(Object source) {

        log.debug("Get Object null Property Names.");
        final BeanWrapper wrappedSource = new BeanWrapperImpl(source);
        return Stream.of(wrappedSource.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(propertyName -> wrappedSource.getPropertyValue(propertyName) == null)
                .toArray(String[]::new);
    }

    public static void copyNotNullProperties(Object src, Object target) {

        log.debug("Copying not null properties.");
        BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
    }

    public static RestTemplate getLoggingBasicAuthenticationRestTemplate() {

        RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        restTemplate.setInterceptors(Arrays.asList(
                new RequestLoggingInterceptor()));
        return restTemplate;
    }
}
