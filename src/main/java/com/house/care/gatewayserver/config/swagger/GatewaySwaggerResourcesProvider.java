package com.house.care.gatewayserver.config.swagger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import lombok.RequiredArgsConstructor;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

/**
 * Retrieves all registered microservices Swagger resources.
 */
@Component
@Primary
@RequiredArgsConstructor
public class GatewaySwaggerResourcesProvider implements SwaggerResourcesProvider {

    private final RouteLocator routeLocator;

    @Override
    public List<SwaggerResource> get() {
        // Add the default swagger resource that correspond to the gateway's own
        // swagger doc

        // Add the registered microservices swagger docs as additional swagger
        // resources
        return Optional.ofNullable(routeLocator.getRoutes()).map(routes1 -> {
            List<SwaggerResource> resources = Lists.newArrayList(swaggerResource("default", "/v3/api-docs"));
            resources
                    .addAll(routes1.stream().map(route -> swaggerResource(route.getId(), route.getFullPath().replace("**", "v3/api-docs"))).collect(Collectors.toList()));
            return resources;
        }).orElseGet(ArrayList::new);
    }

    private SwaggerResource swaggerResource(String name, String location) {

        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion("2.0");
        return swaggerResource;
    }
}
