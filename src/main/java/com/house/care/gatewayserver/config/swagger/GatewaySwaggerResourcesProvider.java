package com.house.care.gatewayserver.config.swagger;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

/**
 * Retrieves all registered microservices Swagger resources.
 */
@Component
@Primary
public class GatewaySwaggerResourcesProvider implements SwaggerResourcesProvider {

    private final RouteLocator routeLocator;

    private final DiscoveryClient discoveryClient;

    public GatewaySwaggerResourcesProvider(RouteLocator routeLocator, DiscoveryClient discoveryClient) {

        this.routeLocator = routeLocator;
        this.discoveryClient = discoveryClient;
    }

    @Override
    public List<SwaggerResource> get() {

        List<SwaggerResource> resources = new ArrayList<>();

        // Add the default swagger resource that correspond to the gateway's own
        // swagger doc
        resources.add(swaggerResource("default", "/v3/api-docs"));

        // Add the registered microservices swagger docs as additional swagger
        // resources
        List<Route> routes = routeLocator.getRoutes();
        routes.forEach(route -> {
            resources.add(swaggerResource(route.getId(), route.getFullPath().replace("**", "v3/api-docs")));
        });

        return resources;
    }

    private SwaggerResource swaggerResource(String name, String location) {

        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion("2.0");
        return swaggerResource;
    }
}
