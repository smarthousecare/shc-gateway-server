package com.house.care.gatewayserver.web.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.house.care.gatewayserver.config.swagger.ServiceDefinitionsContext;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class ServiceDefinitionController {

    private final ServiceDefinitionsContext definitionContext;

    @GetMapping("/service/{serviceName}")
    public String getServiceDefinition(@PathVariable("serviceName") String serviceName) {

        return definitionContext.getSwaggerDefinition(serviceName);
    }
}